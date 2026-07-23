# ADR 0016 — Le registre officiel (`cerbere-core`) est la seule source d'identité device ; `cerbere-devices-mock` s'y aligne par événements

## Statut

Accepted

## Contexte

Le flux initial (ADR 0004) traite `SimulatedDevice` (dans `cerbere-devices-mock`) et `Device` (dans `cerbere-core`) comme deux bounded contexts totalement indépendants, corrélés uniquement par la coïncidence d'un même UUID transporté dans l'événement Kafka. En pratique, cela obligeait l'usager du BO (`cerbere-bff`) à créer le device simulé **et** le device officiel séparément, en s'assurant lui-même que les deux id concordent — exactement le "copier-coller manuel d'UUID" que la note de dette technique du 2026-07-22 (voir CLAUDE.md) identifiait déjà comme un problème à résoudre avant le bridge réel.

Le client a explicitement demandé d'inverser ce sens : le device doit être créé **une seule fois**, dans `cerbere-core` (qui détient l'id, le type, le libellé, la zone), et `cerbere-devices-mock` doit **s'aligner sur les devices déjà créés dans le core** pour choisir lesquels simuler — plutôt que de posséder son propre registre indépendant créé en parallèle.

## Décision

`cerbere-core` reste l'unique source de vérité pour l'identité d'un device (id, type, label, zone) — il l'expose désormais aussi en le publiant. À chaque création/modification/suppression d'un device, `cerbere-core` publie un événement (`device.created` / `device.updated` / `device.deleted`) sur un nouveau topic **`cerbere.device.state`**, via le port `DevicePublisher` et son adapter `DeviceKafkaProducer` (le type n'est jamais transporté sur `device.updated` : il n'est pas modifiable après création, voir `Device`).

`cerbere-devices-mock` consomme ce topic (`DeviceEventKafkaConsumer`) pour maintenir automatiquement son miroir local `SimulatedDevice` (id/type/label/zoneId) synchronisé — sans jamais interroger `cerbere-core` directement (pas de client REST mock → core, l'alternative REST envisagée initialement a été écartée au profit de ce mécanisme événementiel, cohérent avec le reste du système déjà 100% orienté Kafka pour l'inter-service). `SimulatedDevice` ne fait qu'ajouter ce qui est propre à la simulation (`autoSimulate`, `currentState`) par-dessus l'identité reçue du core.

Le BFF (`cerbere-bff`) crée désormais un device en générant lui-même l'id (`UUID.randomUUID()`) et en transmettant type/label/zone à `cerbere-core` — l'usager ne saisit ni ne voit jamais d'UUID. Le device apparaît dans l'écran "Mode test" dès que l'événement `device.created` a été consommé par le mock, sans aucune action supplémentaire côté BO.

**Résilience** : la mise à jour du mock étant asynchrone (délai de propagation Kafka), un événement de device (`cerbere.device.events.raw`, publié par le mock lors d'une simulation) peut arriver côté `cerbere-core` pour un id que le core connaît déjà mais dont le mock a pu avoir un état de propagation différent, ou pour un id supprimé du core entre-temps (le mock n'a pas encore consommé `device.deleted`). `HandleDeviceEventService.handle()` vérifie donc l'existence du device **avant** tout traitement et ignore silencieusement (log niveau INFO) l'événement si le device est inconnu, plutôt que de lever une exception. Côté mock, `DeviceEventKafkaConsumer` encapsule le traitement de chaque message dans un `try/catch` qui logue et ignore toute erreur (type d'événement inconnu, payload incomplet) sans faire échouer le listener ; `UpdateSimulatedDeviceService`/`DeleteSimulatedDeviceService` sont eux-mêmes tolérants à un miroir local absent (mise à jour/suppression d'un device jamais synchronisé : no-op, pas d'exception).

## Conséquences

- Il n'est plus possible de "démarrer la simulation" d'un device qui n'existe pas encore dans `cerbere-core` : l'ordre de création est désormais figé (créer le device dans `cerbere-core` d'abord — via le BFF — puis le piloter dans le mode test) — c'est le comportement voulu, pas une limitation acceptée.
- `cerbere-devices-mock` acquiert une **nouvelle dépendance runtime** vers `cerbere-core`, mais uniquement via Kafka (aucun appel REST direct) : rupture partielle avec la conséquence de l'ADR 0004 selon laquelle le mock ne portait "aucun couplage" avec le registre officiel, mais le couplage reste à sens unique et asynchrone (mock → core via le broker, jamais l'inverse) : `cerbere-core` ignore toujours totalement l'existence de `cerbere-devices-mock`, qui reste donc retirable sans impact sur le reste du système.
- La corrélation par UUID transportée dans l'événement `cerbere.device.events.raw` (ADR 0004) reste inchangée — c'est toujours ce même id qui permet à `cerbere-core` de retrouver le device lors de l'évaluation d'une alerte. Ce qui change, c'est uniquement **où et comment** cet id est choisi : plus par coïncidence de deux saisies indépendantes, mais généré une fois par le BFF et propagé automatiquement.
- Propagation asynchrone assumée : entre la création d'un device côté core et son apparition dans le mode test du BFF, il peut s'écouler le temps de propagation Kafka (de l'ordre de la milliseconde en pratique, infra locale). Aucune UI de "chargement" n'a été ajoutée pour ce délai, jugé négligeable à cette échelle.
- Alternative écartée : un client REST `cerbere-devices-mock → cerbere-core` (`GET /api/devices`) interrogé à la demande — implémenté puis retiré au profit du mécanisme événementiel décrit ci-dessus, jugé plus cohérent avec le reste de l'architecture (100% Kafka pour l'inter-service) et évitant un aller-retour synchrone bloquant à chaque déclenchement de simulation.
