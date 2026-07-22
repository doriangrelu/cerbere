# ADR 0010 — Module Maven `contract` partagé autorisé pour les enveloppes et DTOs

## Statut

Accepted

## Contexte

[ADR 0005](0005-pas-de-librairie-partagee-evenements.md) interdisait toute librairie Java partagée entre microservices pour l'enveloppe d'événement Kafka et les DTOs, au profit d'une duplication volontaire (chaque service porte son propre record). Le client revient sur cette contrainte : il autorise désormais la création d'un module Maven dédié pour décrire les enveloppes de messages et les DTOs partagés, afin d'éviter la duplication et les risques de désynchronisation entre services à mesure que le nombre d'événements et de consommateurs augmente.

## Décision

Un module Maven `cerbere-contract` (nom définitif à confirmer à sa création) **peut** être créé pour porter :
- L'enveloppe d'événement Kafka commune (voir [docs/best-practices/kafka-conventions.md](../best-practices/kafka-conventions.md)).
- Les DTOs partagés entre plusieurs services lorsque leur duplication devient pénible ou risquée (ex : payloads d'événements consommés par plusieurs microservices).

Ce module :
- Est une dépendance **volontaire et explicite** ajoutée au `pom.xml` de chaque service qui en a besoin (pas d'héritage de parent, cohérent avec [ADR 0009](0009-pom-agregateur-racine-optionnel.md)).
- Ne contient **aucune logique métier** ni dépendance à Spring/Mongo/Kafka au-delà de Jackson : uniquement des `record` et enums représentant le contrat de données.
- N'est créé que lorsqu'un **second** service a effectivement besoin de consommer un contrat déjà défini par un premier service — pas de manière anticipée/spéculative. Tant qu'un seul service existe (cas actuel de `cerbere-devices-mock`), chaque module continue de porter son propre DTO local ; l'extraction vers `cerbere-contract` interviendra à la prochaine itération quand `cerbere-core` devra consommer `cerbere.device.events.raw`.

## Conséquences

- Élimine le risque de désynchronisation de format entre producteur et consommateurs pour les contrats extraits dans ce module.
- Introduit un couplage de build volontaire et assumé entre les services qui dépendent de `cerbere-contract` : une évolution incompatible du contrat nécessite de republier ce module puis de mettre à jour la dépendance dans chaque service concerné (bump de version explicite, jamais de `SNAPSHOT` flottant partagé en production).
- Remplace l'approche de [ADR 0005](0005-pas-de-librairie-partagee-evenements.md) (dupliquer, ne jamais partager) par une approche pragmatique : dupliquer tant qu'il n'y a qu'un consommateur, extraire dès qu'il y en a un second.
- Ce module devra lui aussi respecter le [nommage explicite par suffixe](../best-practices/coding-standards.md) (ex : `DeviceEventEnvelope`, suffixe `Envelope`) pour rester lisible depuis n'importe quel service qui l'importe.
