# ADR 0016 — Le registre officiel (`cerbere-core`) est la seule source d'identité device ; `cerbere-devices-mock` s'y réfère

## Statut

Accepted

## Contexte

Le flux actuel (ADR 0004) traite `SimulatedDevice` (dans `cerbere-devices-mock`) et `Device` (dans `cerbere-core`) comme deux bounded contexts totalement indépendants, corrélés uniquement par la coïncidence d'un même UUID transporté dans l'événement Kafka. En pratique, cela obligeait l'usager du BO (`cerbere-bff`) à créer le device simulé **et** le device officiel séparément, en s'assurant lui-même que les deux id concordent — exactement le "copier-coller manuel d'UUID" que la note de dette technique du 2026-07-22 (voir CLAUDE.md) identifiait déjà comme un problème à résoudre avant le bridge réel.

Le client a explicitement demandé d'inverser ce sens : le device doit être créé **une seule fois**, dans `cerbere-core` (qui détient l'id, le type, le libellé, la zone), et `cerbere-devices-mock` doit **lister les devices déjà créés dans le core** pour choisir lesquels simuler — plutôt que de posséder son propre registre indépendant créé en parallèle.

## Décision

`cerbere-core` reste l'unique source de vérité pour l'identité d'un device (id, type, label, zone). `cerbere-devices-mock` n'enregistre plus un device de zéro : son use-case `RegisterSimulatedDeviceUseCase.register(UUID id, boolean autoSimulate)` prend uniquement l'id d'un device **déjà existant côté core**, résout son `type` en interrogeant `cerbere-core` via un nouveau port de sortie (`CoreDeviceLookupPort`, adapter REST `GET {cerbere-core}/api/devices`), et rejette (`DeviceNotFoundException`) tout id ne correspondant à aucun device connu du core.

`SimulatedDevice` ne porte donc plus que ce qui est propre à la simulation : `id`, `type` (résolu depuis le core au moment de l'enregistrement), `autoSimulate`, `currentState`. `label` et `zoneId` sont retirés du modèle — ils restent uniquement dans `cerbere-core`. Le BFF (`cerbere-bff`), qui a déjà les deux clients REST (`DeviceCoreClient`, `DeviceMockClient`), résout label/zone en joignant les deux listes côté contrôleur avant affichage (même pattern que la résolution `zoneId → zoneName` déjà en place, voir docs/best-practices/frontend-conventions.md).

Ce sens de dépendance (`cerbere-devices-mock` → `cerbere-core`, jamais l'inverse) est cohérent avec la vision cible : le jour où `cerbere-devices-mock` sera remplacé par un vrai bridge de devices physiques, ce bridge **devra de toute façon** interroger le registre officiel pour savoir quels devices il pilote — le mock se comporte déjà comme le fera le futur bridge.

## Conséquences

- Il n'est plus possible de "démarrer la simulation" d'un device qui n'existe pas encore dans `cerbere-core` : l'ordre de création est désormais figé (créer le device dans `cerbere-core` d'abord, puis choisir de le simuler dans le mode test du BFF) — c'est le comportement voulu, pas une limitation acceptée.
- `cerbere-devices-mock` acquiert une **nouvelle dépendance runtime** vers `cerbere-core` (config `cerbere.core.base-url`, un `RestClient` de plus) — rupture avec la conséquence de l'ADR 0004 selon laquelle le mock ne portait "aucun couplage" avec le registre officiel. Le couplage reste cependant à sens unique (mock → core) : `cerbere-core` ignore toujours totalement l'existence de `cerbere-devices-mock`, qui reste donc retirable sans impact sur le reste du système.
- La corrélation par UUID transportée dans l'événement Kafka (ADR 0004) reste inchangée — c'est toujours ce même id qui permet à `cerbere-core` de retrouver le device lors de l'évaluation d'une alerte. Ce qui change, c'est uniquement **où et comment** cet id est choisi : plus par coïncidence de deux saisies indépendantes, mais par sélection dans une liste déjà validée par le core.
- Alternative envisagée : synchronisation événementielle (`cerbere-core` publie un événement `device.created` sur Kafka, `cerbere-devices-mock` le consomme pour maintenir sa propre copie locale du registre — un embryon de ce mécanisme existait déjà, non branché, sous forme du record `DeviceCreatedEvent` dans `cerbere-devices-mock`). Écartée pour cette itération : plus complexe (double stockage, réplication à maintenir cohérente) pour un bénéfice marginal vu l'échelle actuelle (lecture à la demande via REST suffit largement). À reconsidérer si le nombre de devices ou la fréquence de consultation devient un problème de performance.
