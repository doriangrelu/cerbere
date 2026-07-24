# ADR 0019 — Module `cerbere-devices-bridge` : supervision du matériel physique via MQTT/Zigbee2MQTT

## Statut

Accepted

## Contexte

Le hardware a été choisi (Zigbee + passerelle Zigbee2MQTT, capteurs Aqara MCCGQ11LM/RTCGQ11LM, sirène pilotée par une prise/relais Zigbee générique — voir [docs/architecture/mqtt-zigbee-contracts.md](../architecture/mqtt-zigbee-contracts.md)). Il faut désormais un module qui supervise ce matériel réel côté Cerbère, en miroir de ce que `cerbere-devices-mock` fait pour la simulation, sans dupliquer ni modifier le contrat déjà établi entre `cerbere-devices-mock` et `cerbere-core`/`cerbere-history`.

Deux problèmes à résoudre proprement : (1) faire correspondre un device physique (identifié côté Zigbee2MQTT par son `friendly_name`) à un device Cerbère (identifié par un UUID généré dans `cerbere-core`) ; (2) faire remonter les événements capteurs (contact/mouvement) vers Kafka et faire descendre les commandes de sirène depuis Kafka vers MQTT, sans exiger le moindre changement dans `cerbere-core`/`cerbere-history`, déjà stables.

## Décision

**Corrélation d'identité** : le `friendly_name` Zigbee2MQTT d'un device **est** l'UUID Cerbère du device — même principe que la corrélation mock/registre officiel (ADR 0004/0016 : "la correspondance se fait uniquement via l'identifiant transporté"). Étape manuelle à l'appairage : renommer le device dans l'interface Zigbee2MQTT pour que son `friendly_name` soit l'UUID généré par le BFF à la création du device dans `cerbere-core`. Aucun nouveau champ sur `Device`/`cerbere-core`/BFF.

**Nouveau module `cerbere-devices-bridge`**, clone architectural de `cerbere-devices-mock` (mêmes couches, mêmes conventions) avec deux différences structurelles : pas de couche REST/webmvc (pur worker Kafka+MQTT, aucun écran de pilotage manuel n'étant nécessaire pour cette itération) ; un miroir local `BridgedDevice` alimenté par `cerbere.device.state` exactement comme `SimulatedDevice`.

**Sens capteur → Kafka** : un listener MQTT (Eclipse Paho, `org.eclipse.paho.mqttv5.client`) s'abonne à `zigbee2mqtt/+`. Pour chaque message reçu, le nom de topic donne l'UUID, le payload est parsé selon le type de device connu dans le miroir local, puis publié sur **`cerbere.device.events.raw`** (topic existant, possédé par `cerbere-devices-mock`, non redéclaré) avec exactement le même `eventType`/`payload` que le mock (`device.contact.state_changed`, `device.motion.detected`) — `cerbere-core`/`cerbere-history` ne voient aucune différence entre un événement issu du mock ou du matériel réel.

**Sens Kafka → commande MQTT** : le bridge devient un 3ᵉ consommateur indépendant de **`cerbere.alarm.state-changed`** (déjà publié par `cerbere-core`, déjà consommé par `cerbere-history`/`cerbere-notification`). Sur `triggered:true`/`false`, il publie `{"state":"ON"}`/`{"state":"OFF"}` sur `zigbee2mqtt/<uuid>/set` pour chaque device de type SIREN connu (`CommandSirenService`, recalcul complet à chaque appel, aucun état intermédiaire mémorisé — même philosophie que `RecomputeZoneViolationService`).

**Résilience identique au mock** : message MQTT dont le `friendly_name` n'est pas un UUID valide ou ne correspond à aucun device connu → log INFO + ignore, jamais d'exception qui romprait la connexion MQTT ou le listener Kafka.

## Conséquences

- Zéro modification requise dans `cerbere-core`/`cerbere-history` pour absorber du matériel réel : les deux topics utilisés (`cerbere.device.events.raw` en écriture, `cerbere.alarm.state-changed` en lecture) existaient déjà et étaient déjà pensés multi-producteurs/multi-consommateurs.
- `cerbere-devices-mock` et `cerbere-devices-bridge` sont mutuellement exclusifs en usage normal (deux sources concurrentes d'événements pour les mêmes devices physiques/simulés) — matérialisé par les profils Docker Compose `mock`/`hardware` (jamais actifs simultanément).
- L'étape manuelle de renommage à l'appairage (`friendly_name` = UUID) est un point de friction opérationnel assumé, cohérent avec le choix déjà fait pour la simulation : pas de nouveau mécanisme de correspondance à inventer, au prix d'une action manuelle par device physique ajouté.
- Alternative écartée : faire porter l'identité par un champ dédié côté `cerbere-core` (ex. `zigbeeFriendlyName` sur `Device`) plutôt que de réutiliser l'UUID comme `friendly_name` — écartée pour rester cohérent avec le principe déjà établi (ADR 0004/0016) et éviter un champ supplémentaire à synchroniser.
- Pas de tests d'intégration MQTT avec un vrai broker (Testcontainers) pour l'instant — seuls les use-cases centraux (`ReportDeviceStateService`, `CommandSirenService`) sont couverts par des tests unitaires purs, même limite déjà assumée côté Kafka (voir dette technique dans `CLAUDE.md`).
