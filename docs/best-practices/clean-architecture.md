# Bonnes pratiques — Clean Architecture par module

Layout canonique appliqué à l'identique dans chaque module Spring Boot Cerbère (voir [ADR 0001](../adr/0001-clean-architecture-par-module.md)). Package racine : `fr.cerbere.component.<module_snake_case>`.

```
domain/
  model/        entités & value objects métier, zéro dépendance framework
  event/        événements de domaine (framework-agnostic), distincts des DTO Kafka
  exception/    exceptions métier
  port/
    in/<agrégat>/   interfaces des use-cases exposés ("driving ports"), ex: port/in/alarm/ArmSystemUseCase
    out/<agrégat>/  interfaces requises par le domaine ("driven ports"), ex: port/out/device/DeviceRepository
application/
  usecase/      implémentations des ports "in", orchestration pure, dépend uniquement de domain
  service/      collaborateurs internes à l'application, n'implémentant aucun port "in" — jamais
                appelés par un adapter, uniquement par d'autres classes application (voir ADR 0018)
infrastructure/
  persistence/mongo/          documents Mongo (@Document) + repositories Spring Data + mappers domain <-> document
  messaging/kafka/producer/   implémentations des ports out via KafkaTemplate
  messaging/kafka/consumer/   @KafkaListener, traduisent le message Kafka en appel de use-case
  messaging/event/            DTO/enveloppe Kafka (records Jackson) + (dé)sérialisation
  client/                     adapters RestClient vers d'autres services, si nécessaire
adapter/
  in/web/       contrôleurs REST, DTO request/response, mappers vers/depuis le domaine
  config/       @Configuration Spring (security, kafka, mongo, scheduling, wiring des beans de port)
```

## Règle de dépendance

```
adapter, infrastructure  --->  application  --->  domain  --->  (rien)
```

Aucune classe de `domain` n'importe `org.springframework.*`, `com.mongodb.*` ou `org.apache.kafka.*`. Si une telle dépendance semble nécessaire dans `domain`, c'est le signe qu'un port manque.

## Sous-packages des ports

Dès qu'un module a plus de 3-4 interfaces de port dans `port/in` ou `port/out`, les regrouper en sous-packages par agrégat/concept métier (ex : `device`, `zone`, `alarm`), plutôt que de tout laisser dans un seul package fourre-tout. Objectif : pouvoir naviguer directement vers "tout ce qui concerne les devices" sans avoir à lire les noms de fichiers un par un. Exemple (`cerbere-core`) :

```
port/in/device/  RegisterDeviceUseCase, UpdateDeviceUseCase, DeleteDeviceUseCase, ListDevicesUseCase
port/in/zone/    RegisterZoneUseCase, UpdateZoneUseCase, DeleteZoneUseCase, ListZonesUseCase
port/in/alarm/   ArmSystemUseCase, DisarmSystemUseCase, GetAlarmStatusUseCase, HandleDeviceEventUseCase
port/out/device/ DeviceRepository
port/out/zone/   ZoneRepository
port/out/alarm/  AlarmSystemRepository, AlarmStateChangedPublisher, AlertPublisher
```

Un module simple avec seulement 2-3 use-cases au total (ex : `cerbere-devices-mock`) peut rester en `port/in`/`port/out` plats — ne pas sur-découper prématurément.

## `application.usecase` vs `application.service`

Un port `port/in/*UseCase` est un **driving port** : quelque chose qu'un adapter (contrôleur REST, `@KafkaListener`, scheduler) invoque directement. Toute logique applicative qui n'est jamais appelée que par une *autre* classe `application` — typiquement pour maintenir un invariant suite à l'effet de bord d'un vrai use-case (ex : recalculer l'état d'un agrégat voisin) — n'est **pas** un use-case et ne doit pas vivre dans `application.usecase` ni prétendre implémenter un `port/in` (voir ADR 0018). Ces collaborateurs vivent dans `application.service`, restent des classes concrètes `final` sans interface (le layer applicatif ne franchit pas la frontière du hexagone en se dépendant lui-même), et sont câblés dans une configuration Spring dédiée (`ApplicationServiceConfig`, distincte de `UseCaseConfig`).

Test rapide pour trancher : *"un adapter appelle-t-il ça directement ?"* — oui → `application.usecase` + `port/in`. Non, seulement une autre classe `application` → `application.service`, pas de port.

## Cas particuliers

- **`api-gateway`** : pas de logique métier, donc pas de `domain`/`application`. Seul `adapter/config` existe (routes Spring Cloud Gateway, sécurité). Écart documenté et assumé.
- **`cerbere-devices-mock`** : ajoute `adapter/in/scheduler/` pour le job périodique de simulation (`@Scheduled`), qui appelle le même use-case que le contrôleur REST de déclenchement manuel — un seul chemin de vérité pour publier un événement.

## Comment ajouter une fonctionnalité

1. Modéliser/étendre le `domain` (entité, value object, exception si besoin).
2. Définir ou étendre un port `in` (le "quoi") et les ports `out` nécessaires (le "de quoi j'ai besoin").
3. Implémenter le use-case dans `application`, contre les interfaces de port uniquement.
4. Implémenter les adapters `infrastructure`/`adapter` qui appellent ce use-case ou fournissent les ports `out`.
5. Câbler avec Spring (injection via les interfaces, jamais les implémentations concrètes en dépendance directe entre couches).
