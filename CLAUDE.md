# CLAUDE.md — Cerbère

Ce fichier est maintenu par l'assistant IA au fil des itérations. Voir la section [Règles de mise à jour](#règles-de-mise-à-jour-pour-lia) en bas de fichier.

## Vue d'ensemble

Cerbère est un système d'alarme domestique développé de zéro (usage personnel, une seule maison), **open source**. Backend microservices Java/Spring Boot, MongoDB, Kafka. Deux interfaces prévues à terme (réseau interne / internet), non développées pour l'instant — voir [docs/prd/000-vision-produit.md](docs/prd/000-vision-produit.md). Vue d'entrée du projet : [README.md](README.md).

Itération en cours : **fondations documentaires + module `cerbere-devices-mock`** (backend uniquement, devices mockés). Détail : [docs/prd/001-mvp-backend-alarme.md](docs/prd/001-mvp-backend-alarme.md).

## Architecture

Vue complète et schéma : [docs/architecture/vue-ensemble.md](docs/architecture/vue-ensemble.md).

Résumé : `api-gateway` (routage) → `cerbere-bff` (agrégation REST) → `cerbere-core`/`cerbere-history` (REST). Communication événementielle via Kafka entre `cerbere-devices-mock` → `cerbere-core`/`cerbere-history`, et `cerbere-core` → `cerbere-history`/`cerbere-notification`.

## Stack technique

- Java 25, Spring Boot 4.1.0 (`spring-boot-starter-parent` en parent de chaque module).
- MongoDB (souplesse de schéma) — une base logique par service, voir [ADR 0003](docs/adr/0003-mongodb-database-per-service.md).
- Kafka (message broker inter-microservices) — conventions dans [docs/best-practices/kafka-conventions.md](docs/best-practices/kafka-conventions.md).
- Spring Cloud Gateway (MVC/servlet, pas WebFlux) pour `api-gateway`.
- Lombok, Spring DevTools, Spring Boot Docker Compose support.
- Pas de pom agrégateur héritant — chaque module a son propre `spring-boot-starter-parent`. Un pom racine `pom` (reactor uniquement, sans héritage) existe pour builder tous les modules ensemble — voir [ADR 0009](docs/adr/0009-pom-agregateur-racine-optionnel.md).
- Sécurité (Keycloak) : dépendances déjà présentes dans les poms, non activées. Voir [ADR 0007](docs/adr/0007-authentification-keycloak-phase-2.md).
- Publication : chaque module publie son package sur GitHub Packages (`distributionManagement` dans chaque pom, workflow `.github/workflows/publish-packages.yml`) — voir [ADR 0011](docs/adr/0011-publication-github-packages.md).

## Conventions de code

Détail complet : [docs/best-practices/coding-standards.md](docs/best-practices/coding-standards.md) et [docs/best-practices/clean-architecture.md](docs/best-practices/clean-architecture.md).

Résumé : clean architecture stricte par module (`domain`/`application`/`infrastructure`/`adapter`), SOLID, `final` systématique (paramètres, variables locales, champs — y compris `this.` obligatoire sur tout accès à un champ/méthode d'instance), records pour les value objects/événements/DTOs, suffixe systématique par rôle (`UseCase`/`Service`/`Repository`/`Document`/`Envelope`/... — voir tableau dans coding-standards.md), JavaDoc obligatoire sur `domain`, pas de commentaires qui répètent le code. Un module `contract` partagé (records/DTOs communs) est autorisé entre services quand un second consommateur en a besoin — voir [ADR 0010](docs/adr/0010-module-contract-partage-autorise.md).

## Structure du repo

| Module | Rôle | Statut |
|---|---|---|
| `api-gateway` | Routage/sécurité (Spring Cloud Gateway) | Scaffold vide |
| `cerbere-bff` | Agrégation REST pour les futures interfaces | Scaffold vide |
| `cerbere-core` | Domaine alarme : zones, devices, arm/disarm, alertes | Scaffold vide |
| `cerbere-history` | Historique des événements/alertes | Scaffold vide |
| `cerbere-notification` | Envoi d'alertes (email/push) | Scaffold vide |
| `cerbere-devices-mock` | Simulation de devices (contact, mouvement, sirène) | **Implémenté** |
| `deployment` | `docker-compose.yaml` (Mongo, Kafka, apps) | Mongo + Kafka (config KRaft corrigée) |
| `docs` | PRD, ADR, bonnes pratiques, architecture | À jour |
| `.github/workflows` | CI de publication GitHub Packages | `publish-packages.yml` (6 modules) |
| `README.md` | Vue d'entrée du projet (open source) | À jour |

## Lancer en local

```bash
# Infra (Mongo + Kafka)
cd deployment && docker compose up -d mongodb kafka

# cerbere-devices-mock
cd cerbere-devices-mock && ./mvnw spring-boot:run
```

Les autres modules ne sont pas encore implémentés (scaffolds vides).

## Journal des itérations

| Date | Itération | Livré | Références |
|---|---|---|---|
| 2026-07-22 | Fondations + devices-mock | Structure `docs/` complète (PRD, 9 ADR, best-practices, vue d'architecture), `CLAUDE.md` racine, module `cerbere-devices-mock` complet (clean architecture, simulation contact/mouvement/sirène, producteur Kafka, scheduler + API de contrôle), correction `deployment/docker-compose.yaml` (renommage + config Kafka KRaft) | [PRD 001](docs/prd/001-mvp-backend-alarme.md), ADR 0001-0009 |
| 2026-07-22 | Règles additionnelles | Module `contract` partagé autorisé (ADR 0010, supersede ADR 0005), suffixe systématique par rôle + `this.` obligatoire (coding-standards.md), renommage `PublishDeviceEventUseCase` → `PublishDeviceEventService`, config GitHub Packages sur les 6 modules + workflow CI (ADR 0011), `README.md` racine, correction du conflit de bean `ObjectMapper`/`JsonMapper` (`@Primary` retiré de `MapperConfig`) qui empêchait le démarrage | ADR 0010, ADR 0011 |

## Décisions d'architecture actives

- [ADR 0001](docs/adr/0001-clean-architecture-par-module.md) — Clean architecture par module (Accepted)
- [ADR 0002](docs/adr/0002-enveloppe-evenement-et-topics-kafka.md) — Enveloppe d'événement et topics Kafka (Accepted)
- [ADR 0003](docs/adr/0003-mongodb-database-per-service.md) — MongoDB database-per-service (Accepted)
- [ADR 0004](docs/adr/0004-module-devices-mock-et-separation-du-registre-device.md) — Module devices-mock séparé (Accepted)
- [ADR 0005](docs/adr/0005-pas-de-librairie-partagee-evenements.md) — Pas de lib partagée pour les événements (Accepted)
- [ADR 0006](docs/adr/0006-notification-copie-locale-des-destinataires.md) — Copie locale des destinataires notification (Accepted)
- [ADR 0007](docs/adr/0007-authentification-keycloak-phase-2.md) — Authentification Keycloak (**Proposed**, phase 2)
- [ADR 0008](docs/adr/0008-strategie-images-cloud-native-buildpacks.md) — Images via Cloud Native Buildpacks (Accepted)
- [ADR 0009](docs/adr/0009-pom-agregateur-racine-optionnel.md) — Pom agrégateur racine sans héritage (Accepted)
- [ADR 0010](docs/adr/0010-module-contract-partage-autorise.md) — Module contract partagé autorisé (Accepted, supersede ADR 0005)
- [ADR 0011](docs/adr/0011-publication-github-packages.md) — Publication GitHub Packages (Accepted)

## Dette technique connue / TODO

- Keycloak non branché : tous les services actuellement en sécurité `permitAll` avec `TODO Keycloak` explicite — ne pas exposer sur internet en l'état.
- `cerbere-core`, `cerbere-history`, `cerbere-notification`, `cerbere-bff`, `api-gateway` restent des scaffolds vides — prochaine itération.
- `deployment/docker-compose.yaml` ne contient encore que Mongo + Kafka + `cerbere-devices-mock` — les 5 autres services applicatifs et le conteneur SMTP factice (maildev) seront ajoutés avec `cerbere-notification`.
- Pas de tests d'intégration Kafka avec un vrai broker (embedded/Testcontainers) pour l'instant — tests unitaires sur les use-cases uniquement.

## Règles de mise à jour pour l'IA

- À chaque itération : ajouter une ligne au **Journal des itérations** ci-dessus (date, ce qui a été livré, liens PRD/ADR).
- Toute décision d'architecture structurante (nouveau choix technique, nouveau pattern, changement de convention) → créer un nouvel ADR dans `docs/adr/`, le référencer dans ce fichier.
- Mettre à jour le tableau **Structure du repo** dès qu'un module change de statut (scaffold → implémenté).
- Mettre à jour `docs/architecture/vue-ensemble.md` si le schéma de communication entre services change.
- Ne jamais laisser ce fichier décrire du code qui n'existe pas encore comme s'il existait — utiliser "À venir"/"Scaffold vide" explicitement.
