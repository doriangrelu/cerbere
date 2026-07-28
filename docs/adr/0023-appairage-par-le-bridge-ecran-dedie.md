# ADR 0023 — L'appairage passe par `cerbere-devices-bridge`, sur un écran dédié

## Statut

Accepted (supersède le canal d'appairage d'[ADR 0021](0021-mock-faux-device-zigbee2mqtt-bridge-seul-canal-appairage.md) : le renommage n'est plus déclenché par une API REST du Mock ; le reste d'ADR 0021 — le Mock comme faux device Zigbee2MQTT, le Bridge comme seul traducteur Kafka — reste inchangé)

## Contexte

L'ADR 0021 a fait du Mock un faux device Zigbee2MQTT, mais a laissé l'appairage (renommage du `friendlyName`) exposé en REST **par le Mock lui-même**, piloté depuis l'écran Mode test du BFF. Retour utilisateur : c'est une erreur de conception. L'appairage est une opération de production — quand le système tournera avec du vrai matériel, il n'y aura pas de Mock pour l'assurer, et l'écran Mode test n'existera même pas (`cerbere.bff.test-mode.enabled=false`). Le seul composant qui dialogue avec le matériel, dans les deux modes, est `cerbere-devices-bridge` : c'est lui qui doit porter l'appairage.

Par ailleurs, deux régressions constatées en recette, de même cause : l'écran Mode test conditionnait le formulaire de déclenchement d'événement à l'état « apparié » (`linked`, voir ADR 0022), lui-même acquis seulement après réception d'un premier événement. Blocage circulaire — impossible de déclencher un événement, donc impossible de devenir apparié, donc le bouton n'apparaissait jamais et l'appairage semblait sans effet. La cause profonde est la même que le problème de conception : mélanger le pilotage du faux matériel et l'appairage sur un même écran a fait dépendre le premier du second, alors qu'un device physique émet sur MQTT indépendamment de ce que le registre officiel sait de lui.

## Décision

**L'appairage passe exclusivement par `cerbere-devices-bridge`**, via l'API bridge standard de Zigbee2MQTT : le Bridge publie sur `<base-topic>/bridge/request/device/rename` un payload `{"from": "<friendly_name courant>", "to": "<uuid du device officiel>"}` (voir [docs/architecture/mqtt-zigbee-contracts.md](../architecture/mqtt-zigbee-contracts.md)). Le destinataire est la vraie passerelle Zigbee2MQTT en profil `hardware`, ou `cerbere-devices-mock` — qui joue désormais aussi ce rôle de passerelle, en plus de celui de device — en profil `mock`. **Le Bridge ne fait aucune différence entre les deux** : c'est exactement le même code, le même topic et le même payload qui servent en test et en production.

**Découverte des devices appairables.** Le Bridge tient un registre `DiscoveredDevice` (persisté) alimenté par son propre listener MQTT : tout message reçu sur `<base-topic>/+` dont le `friendly_name` ne correspond à aucun `BridgedDevice` connu est enregistré comme candidat à l'appairage, au lieu d'être ignoré comme auparavant. Le type est **inféré de la forme du payload** (`contact` → CONTACT, `occupancy` → MOTION, `state` → SIREN) pour aider l'usager à choisir la bonne cible. Le sous-topic réservé `bridge` (API de la passerelle elle-même) est explicitement exclu. Conséquence assumée : **on ne peut apparier qu'un device qui a déjà parlé** — ce qui est le bon critère, un device muet ne serait de toute façon pas exploitable.

**Nouvelle couche web sur le Bridge.** L'ADR 0019 avait acté « pas de REST dans ce module » (pur worker Kafka+MQTT). Cette décision est révisée : le Bridge expose désormais `GET /api/devices-bridge/discovered-devices` et `POST /api/devices-bridge/discovered-devices/pair`, consommés par le BFF. C'est le prix à payer pour que l'appairage soit porté par le seul composant qui parle au matériel.

**Écran dédié dans le BFF.** Nouveau menu **Appairage** (`/pairing`), toujours disponible (contrairement au Mode test, conditionné par un flag) : il liste les devices découverts par le Bridge et permet de les rattacher à un device du registre officiel non encore apparié (`!linked`, voir ADR 0022). L'écran Mode test perd toute notion d'appairage et redevient ce qu'il doit être : le pilotage du faux matériel (créer un device brut, déclencher un état) — **sans condition d'appairage**, ce qui lève le blocage circulaire.

Le `friendlyName` est ici volontairement affiché à l'usager, par exception à la règle « jamais d'UUID/identifiant technique visible » (voir `frontend-conventions.md`) : c'est l'identité que porte physiquement le device côté Zigbee2MQTT, et le seul repère permettant de reconnaître le matériel qu'on vient d'appairer.

## Conséquences

- Le parcours d'appairage est désormais identique en test et en production : un device se signale (émission MQTT) → il apparaît dans l'écran Appairage → on le rattache à un device du registre → le Bridge demande le renommage → le device émet sous son nouveau nom → `cerbere-core` le marque `linked` au premier événement reçu (ADR 0022). Aucune étape ne dépend du Mock.
- `cerbere-devices-mock` n'expose plus d'endpoint REST de renommage : il ne reste que la création d'un device orphelin et le déclenchement d'état (pilotage du faux matériel). Le renommage lui arrive par MQTT, comme à une vraie passerelle.
- Le Mock cumule maintenant deux rôles (device *et* passerelle pour la requête de renommage). C'est une entorse assumée à la fidélité du modèle — dans la réalité ce sont deux composants distincts — mais elle évite d'écrire un faux Zigbee2MQTT complet pour un seul type de message, et reste invisible du Bridge.
- Le Bridge gagne une couche web et donc une surface d'exposition supplémentaire (toujours en `permitAll`, voir la dette technique Keycloak) : à couvrir au même titre que les autres modules lors de la phase 2.
- La découverte repose sur l'observation du trafic MQTT, pas sur le topic retenu `bridge/devices` que publie la vraie passerelle Zigbee2MQTT. Choix délibéré : ça fonctionne à l'identique avec le Mock (qui n'a pas à publier ce topic) et ne dépend d'aucune donnée retenue par le broker. À reconsidérer si l'on veut un jour lister aussi les devices appairés au coordinateur mais encore muets.
- Pas de test d'intégration MQTT bout-en-bout de l'appairage (Bridge → rename → Mock → réémission) : seul le use-case central (`PairDiscoveredDeviceService`) est couvert par des tests unitaires purs, cohérent avec la limite déjà assumée pour le reste du flux MQTT.
