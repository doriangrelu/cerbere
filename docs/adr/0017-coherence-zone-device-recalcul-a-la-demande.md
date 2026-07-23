# ADR 0017 — Cohérence `Zone`/`Device` : recalcul à la demande, jamais de miroir maintenu par écriture

## Statut

Accepted

## Contexte

`Zone` et `Device` sont deux agrégats distincts persistés séparément (deux collections Mongo). La relation d'appartenance est portée par `Device.zoneId` (le device connaît sa zone). `Zone` tentait en parallèle de maintenir sa propre collection inverse `deviceIds`, mise à jour via `Zone.withDeviceAdded`/`withDeviceRemoved`.

En pratique, ce miroir n'a jamais été câblé : `RegisterDeviceService`, `UpdateDeviceService` et `DeleteDeviceService` n'ont jamais eu de dépendance vers `ZoneRepository`, donc `Zone.deviceIds` restait figé à vide depuis `Zone.register()`. Conséquence concrète repérée en test : `DeleteZoneService` vérifie `ZoneNotEmptyException` sur ce `deviceIds` toujours vide, donc une zone réellement occupée pouvait être supprimée sans jamais déclencher le 409 attendu, et le nombre de devices affiché côté BFF était toujours à 0.

Par ailleurs, une nouvelle demande apparaît : afficher un état de violation au niveau de la zone (pas seulement au niveau du device), pour visualiser en un coup d'œil quelle zone déclenche l'alarme — avec la même exigence de persistance que `Device.violation` (`@Version` déjà en place sur les deux agrégats, voir journal CLAUDE.md du 2026-07-23).

Reproduire pour `violation` le même schéma que `deviceIds` (un miroir sur `Zone`, mis à jour en écriture depuis chaque use-case `Device`) aurait reproduit exactement la même classe de bug : plusieurs points d'écriture indépendants (register/update/delete/traitement d'événement/armement), donc plusieurs occasions d'oublier un point de synchronisation.

## Décision

Deux principes, appliqués aux deux besoins :

1. **`Zone` ne possède plus de collection inverse `deviceIds`.** Le champ, ainsi que `withDeviceAdded`/`withDeviceRemoved`, sont supprimés de `Zone`, `ZoneDocument`, `ZoneMapper`, `ZoneResponse` et `ZoneWebMapper`. Toute question du type "combien/quels devices dans cette zone" se répond en interrogeant `DeviceRepository.findByZoneId(zoneId)` (port déjà existant, jusque-là inutilisé) au moment où la réponse est nécessaire — jamais via un champ recopié sur `Zone`. `DeleteZoneService` est corrigé pour interroger `DeviceRepository.findByZoneId(id)` plutôt que `Zone.getDeviceIds()`.

2. **`Zone.violation` reste un champ persisté** (répond au besoin d'un état zone consultable sans reconstituer la liste des devices à chaque lecture, et alignée avec `@Version`), **mais n'est jamais incrémenté/décrémenté** : elle est **entièrement recalculée** à partir d'une requête live sur `DeviceRepository.findByZoneId(zoneId)`, en ne retenant que les devices actifs (`filter(Device::isEnabled).anyMatch(Device::isViolation)` — même filtre que celui déjà appliqué par `AlarmSystemService.arm()`, pour qu'un device désactivé ne fasse jamais passer sa zone en violation), via un unique use-case dédié `RecomputeZoneViolationUseCase`/`RecomputeZoneViolationService` (package `zone`, connaît à la fois `ZoneRepository` et `DeviceRepository` — c'est le seul endroit du code autorisé à faire le pont entre les deux agrégats). Ce use-case est appelé après chaque sauvegarde qui peut changer la violation d'un device ou son rattachement à une zone :
   - `HandleDeviceEventService` (après avoir persisté le nouveau `Device.violation`) ;
   - `UpdateDeviceService` (recalcule l'ancienne zone **et** la nouvelle si `zoneId` change) ;
   - `DeleteDeviceService` (recalcule la zone de rattachement avant suppression).

   `RegisterDeviceService` n'a pas besoin d'appeler ce use-case : un device nouvellement créé démarre toujours avec `violation=false` (`Device.register`), il ne peut donc jamais faire passer une zone en violation à la création.

Le device et la zone restent deux agrégats séparés (pas de fusion, pas de référence croisée dans le domaine) : l'orchestration inter-agrégats vit exclusivement dans la couche `application`, jamais dans `Zone` ni dans `Device` eux-mêmes.

## Conséquences

- Plus de miroir à oublier de synchroniser : la source de vérité (`Device.zoneId`, `Device.violation`) est unique, tout le reste se déduit par requête. La classe de bug qui a touché `deviceIds` ne peut plus se reproduire de la même façon pour `violation`.
- `DeleteZoneService` gagne une dépendance vers `DeviceRepository` ; `UpdateDeviceService`/`DeleteDeviceService` gagnent une dépendance vers `RecomputeZoneViolationUseCase`. Coût accepté : quelques requêtes Mongo supplémentaires par mutation, négligeable à l'échelle d'une maison individuelle.
- `ZoneResponse` (contrat REST partagé) perd `deviceIds` et gagne `violation` (boolean) — impact sur `cerbere-bff` : la colonne "Devices" de l'écran Zones se calcule désormais côté BFF à partir de la liste `DeviceResponse` déjà chargée pour l'écran Devices (agrégation par `zoneId`), plutôt que d'être lue directement sur `ZoneResponse`.
- Alternative écartée : maintenir `deviceIds` en écriture incrémentale (ajouter la dépendance `ZoneRepository` manquante dans chaque use-case `Device`) — corrige le bug immédiat mais reconduit le même risque structurel (un nouveau point d'écriture oublié demain) et ne réglait de toute façon pas le besoin de `violation`, qui aurait posé exactement le même problème une seconde fois.
- Alternative écartée pour `violation` : la calculer uniquement à la lecture (dans `ZoneWebMapper`/`ZoneController`, sans la persister) — plus simple encore et sans aucun risque de désynchronisation, mais rejetée ici car le client souhaite explicitement un état persistant au même titre que `Device.violation` (cohérent avec le verrouillage optimiste déjà en place sur les deux agrégats).
