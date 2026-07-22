# ADR 0012 — Sérialiseur Kafka écrit à la main (Jackson 3)

## Statut

Accepted

## Contexte

Spring Boot 4.1 utilise Jackson 3 (`tools.jackson.databind`) par défaut. `spring-kafka` 3.3.8 ne propose encore que `org.springframework.kafka.support.serializer.JsonSerializer`, basé sur Jackson 2 (`com.fasterxml.jackson.databind`), absent du classpath du module. Résultat : `KafkaException: Failed to construct kafka producer` (`ClassNotFoundException: com.fasterxml.jackson.databind.JavaType`) au premier envoi.

## Décision

Écrire un sérialiseur Kafka minimal (`DeviceEventEnvelopeSerializer`, package `infrastructure.messaging.kafka.producer`) implémentant `org.apache.kafka.common.serialization.Serializer<DeviceEventEnvelope>` avec un `tools.jackson.databind.json.JsonMapper` interne, utilisé à la place du `JsonSerializer` de spring-kafka.

## Conséquences

- Pas de dépendance Jackson 2 ajoutée juste pour spring-kafka.
- Chaque nouveau producteur Kafka (futurs services) devra faire de même tant que spring-kafka ne propose pas de variante Jackson 3 — à revoir dès qu'une version de spring-kafka le fera nativement.
- Alternative écartée : ajouter `com.fasterxml.jackson.core:jackson-databind` en dépendance pour utiliser le `JsonSerializer` fourni — rejeté pour ne pas faire cohabiter deux versions majeures de Jackson dans le même module.
