# ADR 0021 — Le Mock devient un faux device Zigbee2MQTT ; le Bridge est le seul canal d'appairage et de traduction Kafka

## Statut

Accepted (supersède partiellement [ADR 0020](0020-binding-manuel-devices-mock-et-supervision-de-vie.md) : le mécanisme de binding par ré-identification Kafka y décrit est retiré, remplacé par le renommage de `friendlyName` décrit ici ; le reste d'ADR 0020 — supervision de vie côté `cerbere-core` — reste inchangé)

## Contexte

ADR 0020 a introduit un binding manuel entre un device simulé orphelin (`cerbere-devices-mock`) et un device du registre officiel (`cerbere-core`), mais en gardant `cerbere-devices-mock` comme second producteur Kafka indépendant de `cerbere-devices-bridge` — les deux modules publiaient chacun sur `cerbere.device.events.raw`, sans jamais se croiser.

Retour utilisateur : d'un point de vue architectural, l'appairage doit passer par le Bridge (seul composant qui dialogue avec le matériel réel), et le Mock doit se comporter comme un **vrai** device Zigbee2MQTT — parler MQTT, pas Kafka — pour que `cerbere-devices-bridge` puisse être testé de bout en bout (traduction MQTT→Kafka pour les capteurs, Kafka→MQTT pour la commande sirène) sans matériel physique.

## Décision

**`cerbere-devices-mock` perd toute dépendance à Kafka** (plus de `spring-kafka`, plus de `DeviceEventPublisher`/`DeviceEventKafkaProducer`/`DeviceEventEnvelopeFactory`, plus de consommation de `cerbere.device.state`) et gagne une dépendance MQTT (Eclipse Paho, même client que `cerbere-devices-bridge`) :

- `MqttStatePublisher` (port `DeviceStatePublisher`) publie l'état d'un device simulé sur `<base-topic>/<friendlyName>` avec **exactement** les mêmes payloads qu'un vrai device Zigbee2MQTT (`ContactSensorPayload`/`MotionSensorPayload`/`SwitchState`, copies locales identiques à celles du bridge — voir docs/architecture/mqtt-zigbee-contracts.md).
- `MqttCommandListener` s'abonne à `<base-topic>/+/set` pour recevoir les commandes de sirène envoyées par le Bridge (lui-même déclenché par `cerbere.alarm.state-changed`), et met à jour l'état local du device simulé en conséquence — permet de vérifier que la commande part bien du Bridge et produit le bon effet.
- `cerbere-devices-bridge` n'est **pas modifié** : il traite les messages MQTT du Mock exactement comme ceux d'un vrai Zigbee2MQTT, sans distinction possible.

**Identité et appairage.** `SimulatedDevice` porte désormais un `friendlyName` (String, mutable) distinct de son `id` (UUID, clé stable Mongo, jamais exposée sur MQTT) — modèle direct de la distinction adresse réseau/friendly_name d'un vrai device Zigbee. À la création, `friendlyName = id` (device orphelin, comme un device Zigbee tout juste appairé, avant renommage). L'appairage devient un simple **renommage** du `friendlyName` (`RenameSimulatedDeviceUseCase`, `POST /api/devices-mock/{id}/rename`) pour qu'il corresponde à l'UUID d'un device du registre officiel — symétrique au renommage manuel du `friendly_name` dans l'interface Zigbee2MQTT documenté pour le matériel réel (docs/architecture/mqtt-zigbee-contracts.md). Le binding par ré-identification Kafka introduit en ADR 0020 (`BindSimulatedDeviceUseCase`, suppression+réinsertion sous un nouvel id Mongo) est retiré : il n'a plus de sens sans producteur Kafka à ré-identifier.

**Propriété du topic.** `cerbere-devices-bridge` devient le seul producteur réel de `cerbere.device.events.raw` (`cerbere-devices-mock` ne publie plus dessus) : il en provisionne désormais le topic (`KafkaTopicConfig`), rôle auparavant tenu par `cerbere-devices-mock` (ADR 0014).

**Docker Compose.** `mosquitto` et `cerbere-devices-bridge` appartiennent désormais aux deux profils (`mock` et `hardware`) : dans les deux cas, le Bridge tourne réellement et parle MQTT au même broker — seule la source des messages change (`cerbere-devices-mock` en profil `mock`, `zigbee2mqtt` + matériel réel en profil `hardware`, mutuellement exclusifs).

**BFF.** L'écran Mode test reste inchangé dans son principe (création d'un device brut, déclenchement manuel, appairage à un device non encore apparié) — seul le vocabulaire et le endpoint changent ("Lier"→"Apparier", `/bind`→`/rename`), et `friendlyName` (jamais l'UUID brut) sert de clé de calcul du badge Lié/Orphelin côté BFF, en comparant aux ids connus de `cerbere-core`.

## Conséquences

- `cerbere-devices-mock` devient un véritable double numérique du matériel Zigbee2MQTT : toute évolution du contrat MQTT (nouveau type de device, nouveau champ de payload) doit désormais être répercutée dans les deux modules (mock et bridge), exactement comme un vrai device et son intégration doivent rester synchronisés — acceptable, cohérent avec l'objectif de fidélité au réel.
- Le Mock ne peut plus fonctionner sans un broker MQTT (Mosquitto) démarré à côté de lui — contrairement à avant où il ne dépendait que de Kafka. Docker Compose reflète cette nouvelle dépendance (profil `mock` inclut désormais `mosquitto` + `cerbere-devices-bridge`).
- Le binding Kafka d'ADR 0020 (re-clé Mongo sous un nouvel id) est entièrement retiré au profit du renommage de `friendlyName`, plus simple (une seule mise à jour de champ, pas de suppression+réinsertion) et directement réutilisable comme modèle mental pour l'appairage matériel réel.
- Pas de test d'intégration MQTT bout-en-bout Mock→Bridge→Kafka avec un vrai broker Mosquitto pour l'instant — seuls les use-cases centraux (`PublishDeviceEventService`, `RenameSimulatedDeviceService`) sont couverts par des tests unitaires purs côté Mock, cohérent avec la limite déjà assumée côté Bridge (ADR 0019).
- Alternative écartée : garder le Mock comme producteur Kafka direct et le Bridge comme un simple relais MQTT optionnel — écartée car elle ne permet pas de tester réellement le Bridge (le chemin de traduction MQTT↔Kafka resterait uniquement exercé par du vrai matériel, jamais par les tests/démos).
