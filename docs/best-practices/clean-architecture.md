# Bonnes pratiques — Clean Architecture par module

Layout canonique appliqué à l'identique dans chaque module Spring Boot Cerbère (voir [ADR 0001](../adr/0001-clean-architecture-par-module.md)). Package racine : `fr.cerbere.component.<module_snake_case>`.

```
domain/
  model/        entités & value objects métier, zéro dépendance framework
  event/        événements de domaine (framework-agnostic), distincts des DTO Kafka
  exception/    exceptions métier
  port/
    in/         interfaces des use-cases exposés ("driving ports"), ex: ArmSystemUseCase
    out/        interfaces requises par le domaine ("driven ports"), ex: AlarmSystemRepository, DeviceEventPublisher
application/
  usecase/      implémentations des ports "in", orchestration pure, dépend uniquement de domain
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

## Cas particuliers

- **`api-gateway`** : pas de logique métier, donc pas de `domain`/`application`. Seul `adapter/config` existe (routes Spring Cloud Gateway, sécurité). Écart documenté et assumé.
- **`cerbere-devices-mock`** : ajoute `adapter/in/scheduler/` pour le job périodique de simulation (`@Scheduled`), qui appelle le même use-case que le contrôleur REST de déclenchement manuel — un seul chemin de vérité pour publier un événement.

## Comment ajouter une fonctionnalité

1. Modéliser/étendre le `domain` (entité, value object, exception si besoin).
2. Définir ou étendre un port `in` (le "quoi") et les ports `out` nécessaires (le "de quoi j'ai besoin").
3. Implémenter le use-case dans `application`, contre les interfaces de port uniquement.
4. Implémenter les adapters `infrastructure`/`adapter` qui appellent ce use-case ou fournissent les ports `out`.
5. Câbler avec Spring (injection via les interfaces, jamais les implémentations concrètes en dépendance directe entre couches).
