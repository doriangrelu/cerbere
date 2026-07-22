# ADR 0014 — Provisionnement explicite des topics Kafka

## Statut

Accepted

## Contexte

Jusqu'ici, le topic `cerbere.device.events.raw` n'était pas déclaré explicitement : il se serait créé au premier `send()` via l'auto-création Kafka (`auto.create.topics.enable`), avec les valeurs par défaut du broker (souvent 1 partition, rétention par défaut du cluster). Cela fonctionne mais laisse le dimensionnement du topic dépendre d'une configuration externe non maîtrisée par le code, et l'auto-création est désactivée sur beaucoup de clusters Kafka en production.

## Décision

Chaque topic est déclaré explicitement via un bean `NewTopic` (Spring Kafka `TopicBuilder`), dans le service producteur (ex : `KafkaTopicConfig` dans `cerbere-devices-mock` pour `cerbere.device.events.raw`). Dimensionnement retenu pour Cerbère (système mono-maison, faible volume) : 3 partitions, 1 réplica (cluster KRaft mono-broker en développement), rétention 3 jours. Détail dans [docs/best-practices/kafka-conventions.md](../best-practices/kafka-conventions.md).

## Conséquences

- Le dimensionnement du topic est versionné avec le code, pas dépendant d'une config de cluster externe ou de valeurs par défaut potentiellement inadaptées.
- Fonctionne aussi bien avec l'auto-création activée (le bean prend le dessus s'il tourne avant le premier message) que désactivée.
- Chaque futur producteur (`cerbere-core` pour `cerbere.alarm.state-changed`/`cerbere.alarm.alerts`) devra déclarer son propre bean `NewTopic` selon la même convention de dimensionnement, à ajuster si le volume réel s'avère différent.
