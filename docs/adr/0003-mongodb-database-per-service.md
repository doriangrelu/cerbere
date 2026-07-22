# ADR 0003 — MongoDB : une base logique par service

## Statut

Accepted

## Contexte

Le client impose MongoDB pour la souplesse de schéma. Plusieurs microservices ont besoin de persistance (`cerbere-core`, `cerbere-history`, `cerbere-notification`, `cerbere-devices-mock`). Il faut décider du niveau d'isolation entre leurs données.

## Décision

Un seul serveur MongoDB (un conteneur, géré dans `deployment/docker-compose.yaml`), mais **une base logique distincte par service** (`spring.data.mongodb.database` différent par service : `cerbere_core`, `cerbere_history`, `cerbere_notification`, `cerbere_devices_mock`). Aucun service ne doit se connecter à la base d'un autre service ni lire ses collections directement — tout échange de données passe par REST (synchrone) ou Kafka (asynchrone).

## Conséquences

- Chaque service reste maître de son propre schéma de données (cohérent avec les bounded contexts de la clean architecture, [ADR 0001](0001-clean-architecture-par-module.md)).
- Coût opérationnel minimal en développement (un seul conteneur Mongo à faire tourner) tout en gardant une isolation logique migrable plus tard vers des instances Mongo séparées par service, sans changer le code applicatif (seule la chaîne de connexion change).
- Alternative écartée : une base Mongo partagée avec des collections préfixées par service — rejetée car elle encourage la tentation d'accéder directement aux collections d'un autre service, cassant l'isolation des bounded contexts.
