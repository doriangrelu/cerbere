# ADR 0025 — Événement applicatif interne : un seul point de réévaluation de l'état d'alarme

## Statut

Accepted

## Contexte

Quatre use-cases de `cerbere-core` modifiaient un device puis devaient **se souvenir** d'en tirer les conséquences sur l'alarme :

| Use-case | Ce qu'il recopiait |
|---|---|
| `HandleDeviceEventService` | `recompute(zone)`, puis sa propre version de « déclencher l'alarme » (`triggerAlarm`, doublon d'`AlarmTriggerReevaluationService.reevaluate()`) |
| `CheckDeviceHeartbeatsService` | `recompute(zone)` + `reevaluate()` |
| `UpdateDeviceService` | `recompute(ancienne zone)`, `recompute(nouvelle zone)`, `reevaluate()` sous condition de réactivation |
| `DeleteDeviceService` | `recompute(zone)` |

Chacun devait donc connaître, en plus de son propre métier, la liste des conséquences à déclencher et la condition exacte dans laquelle les déclencher. Deux défauts, relevés par l'utilisateur en recette :

1. **La logique est dupliquée.** `HandleDeviceEventService.triggerAlarm()` et `AlarmTriggerReevaluationService.reevaluate()` faisaient la même chose selon deux chemins différents — l'un à partir de l'événement reçu, l'autre à partir de l'état persisté.
2. **C'est hors de la responsabilité du use-case.** La supervision de vie doit répondre à une seule question : ce device donne-t-il encore signe de vie. Décider si l'alarme doit sonner n'est pas son affaire.

Le défaut structurant est qu'un cinquième chemin de modification d'un device aurait dû, lui aussi, penser à appeler les deux collaborateurs — sans qu'aucune protection n'existe s'il l'oubliait.

## Décision

Un use-case qui modifie un device n'en tire plus aucune conséquence : il **émet un fait**, et un seul consommateur en dérive l'état d'alarme.

**L'événement.** `DeviceSupervisionChanged(deviceId, affectedZoneIds)` (`domain.event`), émis dès qu'un device change d'une façon susceptible d'influer sur l'alarme : violation, activation, zone de rattachement, suppression. Il ne quitte jamais le process — c'est un signal de cohérence interne, pas un fait historisable, donc ni `eventId` ni `occurredAt` (contrairement à tous les autres événements de `domain.event`, qui partent sur Kafka). `affectedZoneIds` porte les zones à recalculer (deux quand le device change de zone) ; les identifiants nuls sont écartés à la construction pour qu'aucun consommateur n'ait à s'en préoccuper.

**Le transport passe par un port de sortie.** `DeviceSupervisionChangedPublisher` (`port/out/device`), implémenté par `SpringDeviceSupervisionChangedPublisher` (`infrastructure/messaging/spring`), seule classe du module à connaître `ApplicationEventPublisher`. La couche `application` reste dépourvue de type framework, exactement comme pour les producteurs Kafka (ADR 0001).

**La réception est un adapter d'entrée pilotant un vrai port d'entrée.** `DeviceSupervisionChangedListener` (`adapter/in/event`, `@EventListener`) est un adapter au même titre qu'un `@KafkaListener` ou qu'un scheduler ; il appelle `ReevaluateAlarmStateUseCase`. Conformément à l'[ADR 0018](0018-package-application-service-collaborateurs-internes.md), c'est donc bien un `port/in` et non un collaborateur de `application.service` : il est appelé par un adapter, pas par une autre classe `application`.

**`ReevaluateAlarmStateService` est le seul endroit qui dérive l'état d'alarme** : il recalcule la violation des zones concernées, puis réévalue le déclenchement. `RecomputeZoneViolationService` et `AlarmTriggerReevaluationService` restent des collaborateurs internes, mais n'ont plus qu'un seul appelant chacun (`AlarmSystemService.arm()` continue d'utiliser `anyEnabledDeviceViolating()`, qui est une question, pas une conséquence).

**La réévaluation est inconditionnelle et idempotente.** L'appelant n'a plus à juger si son changement « mérite » une réévaluation : `AlarmTriggerReevaluationService.reevaluate()` ne fait rien si le système est désarmé ou déjà déclenché. C'est ce qui permet de supprimer les conditions dispersées (`if (!wasEnabled && enabled)` dans `UpdateDeviceService`, `if (!alarmSystem.isTriggered())` dans `HandleDeviceEventService`).

**Synchrone, sans `try/catch`.** Pas d'`@Async` : la réévaluation doit se terminer avant que l'appelant ne rende la main, sans quoi un déclenchement d'alarme pourrait arriver après la réponse HTTP ou l'accusé de réception Kafka. Et une réévaluation qui échoue doit faire échouer l'opération d'origine plutôt que laisser le système dans un état incohérent — c'est l'inverse du principe appliqué aux consumers Kafka, qui eux ne doivent jamais bloquer le flux entrant.

## Conséquences

- `HandleDeviceEventService` perd `triggerAlarm()` et sa dépendance à `AlarmStateChangedPublisher` : le déclenchement passe désormais par le chemin unique. Comportement inchangé — l'ancien code déclenchait sur « l'événement reçu est une violation », le nouveau sur « un device actif est en violation », équivalent puisque le device vient précisément d'être enregistré comme tel.
- `CheckDeviceHeartbeatsService` ne connaît plus ni les zones ni l'alarme : il décide qui est muet, marque la violation, lève son alerte `WARNING`. C'était la demande initiale.
- `UpdateDeviceService` et `DeleteDeviceService` perdent toute dépendance à `application.service`.
- L'ordre à l'intérieur de la réévaluation reste significatif (zones d'abord, déclenchement ensuite) mais il est écrit **une seule fois**, dans `ReevaluateAlarmStateService`.
- La réévaluation tourne désormais sur *tous* les événements de device, y compris ceux qui ne changent rien (un `findAll()` de plus). Négligeable à l'échelle d'une maison, et le prix de la robustesse : le système se répare tout seul si un état de zone avait dérivé.
- Alternative écartée : injecter directement `ApplicationEventPublisher` dans les use-cases. Plus court, mais fait entrer un type Spring dans `application.usecase`, ce que le projet interdit depuis l'ADR 0001 — le port de sortie coûte une interface et un adapter de six lignes.
- Alternative écartée : faire porter la réévaluation par un `application.service` appelé par les use-cases. C'est exactement la situation d'avant, avec une classe de plus : l'appelant garderait la charge de se souvenir de l'appeler.
