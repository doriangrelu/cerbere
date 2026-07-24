# Contrats MQTT/Zigbee2MQTT — `cerbere-devices-bridge`

Document vivant, à tenir à jour à chaque nouveau type de device physique ajouté (voir règle de mise à jour dans `CLAUDE.md`).

## Vue d'ensemble

`cerbere-devices-bridge` dialogue avec les capteurs/actionneurs physiques via **MQTT**, la passerelle **Zigbee2MQTT** faisant le pont avec le réseau Zigbee (dongle coordinateur). Le bridge ne parle jamais directement Zigbee — uniquement MQTT, exactement comme Zigbee2MQTT l'expose.

```
Capteur Zigbee ──radio──> Coordinateur (dongle USB) ──> Zigbee2MQTT ──MQTT (Mosquitto)──> cerbere-devices-bridge ──Kafka──> cerbere-core / cerbere-history
```

## Structure générale des topics

- `<base-topic>/<friendly_name>` — état publié en JSON par le device (lecture). `<base-topic>` = `zigbee2mqtt` par défaut (`cerbere.devices-bridge.mqtt.base-topic`).
- `<base-topic>/<friendly_name>/set` — commande publiée en JSON pour piloter le device (écriture, sirène uniquement dans ce tour).
- `<base-topic>/<friendly_name>/get` — force une lecture (non utilisé par le bridge pour l'instant).
- `<base-topic>/<friendly_name>/availability` — `online`/`offline` (non consommé pour l'instant — le bridge s'abonne uniquement à `<base-topic>/+`, un seul niveau, qui ne capture donc pas ces sous-topics).

## Règle de corrélation : `friendly_name` = UUID Cerbère

Le `friendly_name` Zigbee2MQTT d'un device **est** l'UUID généré par le BFF à la création du device dans `cerbere-core` (même principe que la corrélation mock/registre officiel — voir ADR 0004/0016). **Étape manuelle à l'appairage** : après avoir créé le device dans l'écran Devices du BFF (qui génère l'UUID), renommer le device correspondant dans l'interface Zigbee2MQTT (`Rename` sur le device fraîchement appairé) pour que son `friendly_name` soit exactement cet UUID (format `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`).

Un message MQTT dont le `friendly_name` n'est ni un UUID valide, ni un device connu du miroir local du bridge, est ignoré silencieusement (log niveau INFO) — jamais d'exception qui romprait la connexion MQTT.

## Contrats par type de device

### CONTACT — Aqara MCCGQ11LM (contact porte/fenêtre)

Topic : `zigbee2mqtt/<uuid>` (lecture seule, pas de `/set` — ce device ne se pilote pas).

Payload réel :
```json
{ "contact": true, "battery": 100, "voltage": 3025, "device_temperature": 22 }
```

| Champ | Type | Signification |
|---|---|---|
| `contact` | boolean | `true` = fermé, `false` = ouvert |
| `battery` | int (0-100) | pourcentage restant |
| `voltage` | int (mV) | tension de la pile CR1632 |
| `device_temperature` | double (°C) | température interne du capteur |

POJO : `ContactSensorPayload(Boolean contact, Integer battery, Integer voltage, Double deviceTemperature)`.

Mapping vers Cerbère : `contact:true` → `ContactState.CLOSED`, `contact:false` → `ContactState.OPEN` → événement Kafka `device.contact.state_changed`, `payload: {"state": "OPEN"|"CLOSED"}` (identique à `cerbere-devices-mock`).

### MOTION — Aqara RTCGQ11LM (détecteur de mouvement)

Topic : `zigbee2mqtt/<uuid>` (lecture seule).

Payload réel :
```json
{ "occupancy": true, "illuminance": 42, "battery": 100, "voltage": 3000 }
```

| Champ | Type | Signification |
|---|---|---|
| `occupancy` | boolean | `true` = mouvement détecté |
| `illuminance` | int (lux) | niveau de luminosité |
| `battery` | int (0-100) | pourcentage restant |
| `voltage` | int (mV) | tension de la pile CR2450 |

Particularité matérielle : après une détection, le capteur ignore tout nouveau mouvement pendant 60 secondes (`occupancy_timeout`, configurable côté Zigbee2MQTT, défaut 90s).

POJO : `MotionSensorPayload(Boolean occupancy, Integer illuminance, Integer battery, Integer voltage)`.

Mapping vers Cerbère : `occupancy:true` → `MotionState.DETECTED`, sinon `CLEAR` → événement Kafka `device.motion.detected`, `payload: {"detected": bool}` (identique à `cerbere-devices-mock`).

### SIREN — prise/relais Zigbee générique

**Pas** de sirène Zigbee dédiée (le modèle grand public documenté, Heiman HS2WD-E, a des retours utilisateurs peu fiables sur son payload `warning` — voir historique de la conversation). Le choix retenu : une prise/relais Zigbee générique pilotant une sirène filaire 12V. Contrat symétrique lecture/écriture :

Topic (lecture) : `zigbee2mqtt/<uuid>` — Topic (commande) : `zigbee2mqtt/<uuid>/set`.

Payload (les deux sens) :
```json
{ "state": "ON" }
```

| Champ | Type | Signification |
|---|---|---|
| `state` | `"ON"` \| `"OFF"` | état du relais |

POJO : `SwitchState(String state)`, constantes `SwitchState.ON`/`SwitchState.OFF`.

Mapping vers Cerbère (lecture) : `state:"ON"` → `SirenState.ACTIVE`, sinon `INACTIVE` → événement Kafka `device.siren.triggered`, `payload: {"active": bool}` (identique à `cerbere-devices-mock`).

Mapping (écriture, commande) : `cerbere-devices-bridge` consomme `cerbere.alarm.state-changed` (déjà publié par `cerbere-core`) ; sur `triggered:true`, publie `{"state":"ON"}` sur `.../set` pour chaque device SIREN connu ; sur `triggered:false`, publie `{"state":"OFF"}`.

## Résilience

Un seul abonnement générique `<base-topic>/+` (un niveau) capture tous les devices sans capter `/set`/`/availability`/`/get`. Tout message dont le `friendly_name` n'est pas un UUID, ou ne correspond à aucun device connu, ou dont le payload est illisible, est loggé (niveau INFO pour "device inconnu", ERROR pour une erreur de traitement inattendue) et ignoré — jamais d'exception qui romprait la connexion MQTT (même principe que `DeviceEventKafkaConsumer` côté `cerbere-devices-mock`, voir ADR 0016).

## Sources

- [MQTT Topics and Messages | Zigbee2MQTT](https://www.zigbee2mqtt.io/guide/usage/mqtt_topics_and_messages.html)
- [Aqara MCCGQ11LM control via MQTT | Zigbee2MQTT](https://www.zigbee2mqtt.io/devices/MCCGQ11LM.html)
- [Aqara RTCGQ11LM control via MQTT | Zigbee2MQTT](https://www.zigbee2mqtt.io/devices/RTCGQ11LM.html)
