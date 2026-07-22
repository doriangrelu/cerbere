# ADR 0004 — Module `cerbere-devices-mock` séparé du registre officiel des devices

## Statut

Accepted

## Contexte

Le client veut mocker la couche devices (contact porte/fenêtre, détecteur de mouvement, sirène) pour cette itération, en attendant du vrai matériel. Il faut décider où vit cette simulation et comment elle s'articule avec le futur registre de devices utilisé par le back-office d'administration (dans `cerbere-core`).

## Décision

Créer un module dédié `cerbere-devices-mock`, dont l'unique responsabilité est de **simuler** des capteurs/actionneurs et de publier leurs événements sur Kafka. Il possède son propre modèle `SimulatedDevice`, volontairement **découplé** du modèle `Device` qui vivra dans `cerbere-core` (registre officiel utilisé pour la configuration BO et l'évaluation d'alarme).

Deux mécanismes de simulation, convergeant vers le même use-case (`PublishDeviceEventUseCase`) pour garantir un format d'événement homogène :
1. Un scheduler périodique (`@Scheduled`) qui déclenche des événements aléatoires sur les devices marqués `autoSimulate=true`.
2. Une API REST de contrôle (`POST /api/devices-mock/{deviceId}/events`) pour déclencher un événement précis à la demande (démo, tests).

## Conséquences

- `cerbere-devices-mock` pourra être **retiré du système sans aucun impact** sur `cerbere-core`/`cerbere-history`/`cerbere-notification` le jour où du vrai matériel sera branché — il ne porte aucune logique métier d'alarme.
- Duplication assumée entre `SimulatedDevice` (simulation) et `Device` (registre officiel) : deux bounded contexts distincts, pas de couplage entre eux au niveau code. La correspondance entre un device simulé et un device du registre se fait uniquement via l'identifiant transporté dans l'événement Kafka.
- Alternative écartée : intégrer la simulation directement dans `cerbere-core` (ex : un flag "mode simulation") — rejetée car elle mélangerait logique métier et logique de test/démo dans le même service, et rendrait plus difficile la suppression propre du mock plus tard.
