# ADR 0020 — Binding manuel des devices simulés orphelins + supervision de vie (heartbeat)

## Statut

Accepted (supersède partiellement [ADR 0016](0016-inversion-dependance-mock-vers-core.md) : le mécanisme d'auto-mirroring sur `device.created` est retiré, le reste d'ADR 0016 — `cerbere.device.state` comme unique canal de synchronisation d'identité, résilience du consommateur — reste inchangé)

## Contexte

Deux retours utilisateur traités ensemble.

**Binding.** Depuis l'ADR 0016, chaque device créé dans `cerbere-core` obtient automatiquement un miroir dans `cerbere-devices-mock` (même id, via l'événement `device.created`). Ce mécanisme convenait à la simulation pure, mais ne reflète pas la réalité du matériel physique : un capteur Zigbee existe et communique (appairage Zigbee2MQTT) **avant** d'être rattaché à un device du registre officiel — voir déjà le renommage manuel du `friendly_name` documenté dans [docs/architecture/mqtt-zigbee-contracts.md](../architecture/mqtt-zigbee-contracts.md) pour `cerbere-devices-bridge`. L'utilisateur veut retrouver ce même principe côté BFF/mock : un device simulé peut exister en orphelin (créé sans lien avec le registre officiel), et un device du registre officiel peut rester non lié jusqu'à ce qu'un opérateur le lie explicitement à un orphelin depuis le BFF.

**Supervision de vie.** Aucun mécanisme ne détectait qu'un device avait cessé de communiquer. L'utilisateur veut qu'un device injoignable depuis un certain temps déclenche l'alarme, au même titre qu'une violation physique.

## Décision

### Binding

`cerbere-devices-mock` cesse de créer automatiquement un miroir sur `device.created` (`DeviceEventKafkaConsumer` : ce cas devient un simple log, plus d'appel à `RegisterSimulatedDeviceUseCase`). À la place :

- Un device simulé peut être créé **orphelin** (`POST /api/devices-mock`, type + libellé seulement, id généré par le mock, pas de zone) — représente un device physique/simulé fraîchement appairé, pas encore rattaché.
- Un nouveau use-case `BindSimulatedDeviceUseCase`/`BindSimulatedDeviceService` (`POST /api/devices-mock/{orphanId}/bind`) lie un orphelin à un device du registre officiel : le miroir change d'identité (id de l'orphelin remplacé par l'id du device officiel), en conservant type/état courant/`autoSimulate` de l'orphelin, mais en adoptant libellé/zone du device officiel (source de vérité pour ces champs, cohérent avec ADR 0016). Refuse si le device officiel cible est déjà lié (`DeviceAlreadyBoundException`, 409).
- **Aucun champ nouveau côté `cerbere-core`** : "lié"/"non lié" n'est jamais persisté dans `Device` — c'est un fait dérivé, calculé côté BFF en comparant les ids connus de `cerbere-core` et de `cerbere-devices-mock` (le BFF interroge déjà les deux services). Même principe que la violation de zone recalculée à la demande (ADR 0017) : pas de miroir qui peut driver.
- BFF : l'écran Mode test affiche le statut Lié/Orphelin de chaque device simulé, propose un formulaire de création orpheline, et un formulaire de liaison (choix parmi les devices officiels non encore liés) pour chaque ligne orpheline. L'id soumis est revalidé côté BFF contre la liste courante avant transmission (même règle que pour `zoneId`/`candidateId`, voir frontend-conventions.md).
- **Portée** : ce mécanisme concerne `cerbere-devices-mock` uniquement dans ce tour (demande explicite). `cerbere-devices-bridge` garde son mécanisme de corrélation propre (renommage du `friendly_name` Zigbee2MQTT, voir ADR 0019) — l'unification éventuelle des deux mécanismes de binding est laissée à une itération ultérieure si besoin.

### Supervision de vie (heartbeat)

`Device` gagne un champ `lastSeenAt` (`Instant`), initialisé à la création (`register()`) et rafraîchi à chaque événement reçu (`HandleDeviceEventService`, avec la date de l'événement plutôt que l'heure de traitement) et à la réactivation d'un device désactivé (`UpdateDeviceService`, grâce d'un nouveau délai plutôt que flag immédiat comme injoignable).

Un nouveau use-case `CheckDeviceHeartbeatsUseCase`/`CheckDeviceHeartbeatsService` (véritable port d'entrée : appelé par un adapter scheduler, voir ADR 0018), déclenché périodiquement par `DeviceHeartbeatScheduler` (`@Scheduled`, propriété `cerbere.core.device-heartbeat.check-interval-ms`), marque en violation tout device actif, pas déjà en violation, dont `lastSeenAt` dépasse le délai configuré (`cerbere.core.device-heartbeat.timeout-ms`) : recalcule la violation de zone (réutilise `RecomputeZoneViolationService`) et réévalue le déclenchement de l'alarme (réutilise `AlarmTriggerReevaluationService`, déjà utilisé pour la réactivation d'un device — même besoin : "un device passe en violation en dehors du chemin `HandleDeviceEventService`, faut-il déclencher l'alarme ?"), puis lève une `AlertRaised` dédiée (sévérité `WARNING`, message explicite "injoignable").

Un device déjà marqué en violation n'est pas retraité à chaque passage du scheduler (pas de ré-alerte en boucle) — il redevient supervisé normalement dès qu'un nouvel événement le fait ressortir de la violation, comme n'importe quelle violation physique.

## Conséquences

- Un device créé dans `cerbere-core` sans jamais être lié à un orphelin mock ne recevra jamais d'événement — il sera éventuellement marqué injoignable par la supervision de vie une fois le délai de timeout dépassé depuis sa création (comportement voulu : un device jamais raccordé doit être visible comme problématique, pas silencieusement ignoré).
- `cerbere-devices-mock` perd la garantie "un device créé dans `cerbere-core` a toujours un miroir immédiat" — un consommateur externe qui s'appuyait sur ce miroircomme preuve d'existence devrait désormais vérifier le binding plutôt que la simple présence.
- Le délai de supervision de vie (5 minutes par défaut) est un réglage global, pas par device/type — un capteur à cycle de reporting naturellement long (ex : batterie faible, fréquence réduite) pourrait être faussement marqué injoignable ; à ajuster/spécialiser si besoin dans une itération ultérieure.
- Pas de test d'intégration Kafka/scheduler réel pour la supervision de vie — seuls les use-cases centraux (`BindSimulatedDeviceService`, `CheckDeviceHeartbeatsService`) sont couverts par des tests unitaires purs, cohérent avec le reste du repo.
- Alternative écartée pour le heartbeat : un événement Kafka dédié de battement de cœur, publié périodiquement par le mock/bridge même sans changement d'état physique — écartée pour ne pas introduire de nouveau topic/eventType alors que `lastSeenAt` peut se déduire des événements déjà publiés (`cerbere.device.events.raw`), et pour ne pas complexifier `cerbere-devices-mock`/`cerbere-devices-bridge` d'un mécanisme supplémentaire à maintenir.
