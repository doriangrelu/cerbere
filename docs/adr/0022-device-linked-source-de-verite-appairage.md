# ADR 0022 — `Device.linked` dans `cerbere-core` : seule source de vérité pour l'appairage

## Statut

Accepted

## Contexte

Deux retours utilisateur après la mise en service du Mock-comme-faux-device (ADR 0021).

**Supervision de vie incomplète.** Un device jamais appairé ne levait aucune alerte particulière : `CheckDeviceHeartbeatsService` traitait "jamais entendu depuis la création" exactement comme "a cessé de répondre après avoir fonctionné", en s'appuyant uniquement sur `lastSeenAt` (qui démarre à la création du device, avant tout appairage). Un device créé mais jamais raccordé finissait certes par être marqué en violation après le délai configuré, mais sans qu'on puisse distinguer "jamais raccordé" de "raccordé puis silencieux" — et sans qu'aucune partie du système ne sache explicitement si un device avait *déjà* prouvé qu'il communiquait.

**Le binding géré au mauvais niveau.** Le mécanisme d'appairage (savoir si un device est "lié") était entièrement recalculé côté BFF (`TestModeController`) en croisant deux API REST indépendantes : la liste des devices de `cerbere-core` et celle de `cerbere-devices-mock`. Ce calcul n'a de sens que parce que le Mock existe et expose une API comparable à celle de `cerbere-core` — il ne fonctionnerait pas du tout avec du matériel réel (`cerbere-devices-bridge` n'expose aucune API de ce type, et n'en a pas besoin). Le binding était donc de facto une fonctionnalité de test, pas un concept que `cerbere-core` connaissait lui-même — alors que "ce device communique-t-il ?" est une question métier qui doit avoir une réponse quel que soit le déploiement (test ou matériel réel).

## Décision

`Device` (dans `cerbere-core`) gagne un champ persisté **`linked`** (booléen, `false` à l'enregistrement). Il passe à `true` la toute première fois que `HandleDeviceEventService` accepte un événement pour ce device — quelle que soit la source (Mock ou matériel réel via le Bridge, `cerbere-core` ne fait aucune différence, exactement comme pour le reste du flux). Jamais réinitialisé ensuite : une fois la preuve de communication établie, elle reste acquise, même si le device devient silencieux plus tard (c'est alors un problème de supervision de vie, pas de remise en cause de l'appairage).

**Supervision de vie corrigée** : `CheckDeviceHeartbeatsService` ignore désormais les devices non liés (`!linked`) — un device jamais raccordé n'a jamais eu l'occasion de communiquer, ce n'est pas une violation mais un provisionnement en attente. Seuls les devices déjà liés sont soumis au délai de staleness (`lastSeenAt` vs `cerbere.core.device-heartbeat.timeout-ms`).

**Binding recentré sur `cerbere-core`** : `DeviceResponse`/`DeviceRow` exposent `linked`, avec un badge "Appairé"/"Non appairé" sur l'écran Devices du BFF. L'écran Mode test (`TestModeController`) ne recalcule plus le statut Lié/Orphelin en croisant les deux API : il lit directement `linked` sur le device officiel correspondant au `friendlyName` du device simulé. La liste des devices officiels proposés à l'appairage (`unboundDevices`) se filtre désormais sur `!linked` (core) plutôt que sur l'absence dans la liste des `friendlyName` connus du Mock (dérivé). Le mécanisme de renommage du Mock (`RenameSimulatedDeviceUseCase`, voir ADR 0021) reste inchangé — c'est toujours lui qui déclenche la circulation d'événements — mais ce n'est plus lui qui décide si l'appairage a "réussi" : cette décision appartient exclusivement à `cerbere-core`, qui l'observe directement.

## Conséquences

- Le badge Lié/Orphelin de l'écran Mode test ne passe à "Lié" qu'une fois qu'un événement a réellement traversé le Bridge (ou été reçu du Mock) et a été accepté par `cerbere-core` — pas au simple renommage du `friendlyName`. C'est un changement de comportement volontaire : renommer sans jamais déclencher d'événement laisse désormais le device visiblement "Orphelin", ce qui est plus honnête et permet de vraiment vérifier que la communication fonctionne (l'objectif même du Mock-comme-faux-device, ADR 0021).
- Ce mécanisme fonctionne identiquement pour le matériel réel : dès qu'un vrai device Zigbee correctement renommé dans Zigbee2MQTT envoie son premier état, `cerbere-core` le marque `linked` sans qu'aucun code spécifique au matériel réel n'ait été nécessaire — le binding n'est plus une fonctionnalité du Mock, c'est une propriété générale de `cerbere-core`.
- Un device désactivé puis réactivé reste `linked` s'il l'était déjà (le drapeau n'est jamais remis à faux) — seule la supervision de vie (`lastSeenAt`) bénéficie d'une grâce à la réactivation (voir ADR 0020), l'appairage lui-même n'est jamais remis en cause.
- Pas de nouvel endpoint ni de nouveau topic : `linked` est dérivé du flux d'événements déjà existant (`cerbere.device.events.raw`), cohérent avec le principe déjà appliqué pour `lastSeenAt` (ADR 0020).
- Alternative écartée : garder le calcul croisé côté BFF mais le généraliser à une future API du Bridge — écartée car elle aurait nécessité de dupliquer une notion métier (l'appairage) dans chaque module transportant des devices physiques/simulés, alors que la question relève entièrement de `cerbere-core` (seul dépositaire du registre officiel).
