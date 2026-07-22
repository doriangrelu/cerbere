# ADR 0001 — Clean architecture identique dans chaque module

## Statut

Accepted

## Contexte

Cerbère est composé de plusieurs microservices Spring Boot indépendants (`api-gateway`, `cerbere-bff`, `cerbere-core`, `cerbere-history`, `cerbere-notification`, `cerbere-devices-mock`). Le client exige une architecture propre, testable, avec une séparation stricte entre logique métier et détails techniques (Mongo, Kafka, HTTP), et l'application rigoureuse des principes SOLID.

## Décision

Chaque module applique la même structure de packages sous `fr.cerbere.component.<module_snake_case>` :

```
domain/model, domain/event, domain/exception, domain/port/{in,out}
application/usecase
infrastructure/persistence/mongo, infrastructure/messaging/kafka/{producer,consumer}, infrastructure/messaging/event, infrastructure/client
adapter/in/web, adapter/config
```

Règle de dépendance stricte, vérifiable à la lecture des imports : `domain` ne dépend d'aucun framework ; `application` ne dépend que de `domain` ; `infrastructure` et `adapter` dépendent de `domain` et `application`, jamais l'inverse.

**Exception assumée** : `api-gateway` n'a pas de logique métier — il ne contient que `adapter/config` (routes, sécurité). Il n'a donc pas de `domain`/`application`.

## Conséquences

- La logique métier (domain + application) est testable unitairement sans Spring, sans Mongo, sans Kafka.
- Remplacer Mongo ou Kafka par une autre technologie n'impacte que le package `infrastructure` correspondant.
- Coût : plus de fichiers/interfaces qu'une architecture en couches classique (`controller/service/repository`) — accepté car cohérent avec l'exigence SOLID du client (inversion de dépendance via les ports).
- Alternative écartée : architecture en couches classique Spring (`controller → service → repository`) — plus rapide à écrire mais couple la logique métier à Spring Data/Kafka dès le départ, ce que le client a explicitement demandé d'éviter.
