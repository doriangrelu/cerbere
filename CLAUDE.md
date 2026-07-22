# CLAUDE.md — Cerbère

Ce fichier est maintenu par l'assistant IA au fil des itérations. Voir la section [Règles de mise à jour](#règles-de-mise-à-jour-pour-lia) en bas de fichier.

## Vue d'ensemble

Cerbère est un système d'alarme domestique développé de zéro (usage personnel, une seule maison), **open source**. Backend microservices Java/Spring Boot, MongoDB, Kafka. Deux interfaces prévues à terme (réseau interne / internet), non développées pour l'instant — voir [docs/prd/000-vision-produit.md](docs/prd/000-vision-produit.md). Vue d'entrée du projet : [README.md](README.md).

Itération en cours : **`cerbere-bff`** (front interne BO admin — tableau de bord alarme). Détail : [docs/prd/001-mvp-backend-alarme.md](docs/prd/001-mvp-backend-alarme.md).

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
- Front interne (`cerbere-bff`) : Thymeleaf server-side + [htmx](https://htmx.org/)(-spring-boot) + [Beer CSS](https://www.beercss.com/) (Material Design 3, sans build JS) — voir [ADR 0015](docs/adr/0015-stack-front-interne-thymeleaf-htmx-beercss.md) et [docs/best-practices/frontend-conventions.md](docs/best-practices/frontend-conventions.md). `htmx-spring-boot` n'est **pas** utilisée (incompatible Boot 4.1 pour l'instant) : htmx brut en CDN.

## Conventions de code

Détail complet : [docs/best-practices/coding-standards.md](docs/best-practices/coding-standards.md) et [docs/best-practices/clean-architecture.md](docs/best-practices/clean-architecture.md).

Résumé : clean architecture stricte par module (`domain`/`application`/`infrastructure`/`adapter`), sous-packagée par agrégat dès qu'un `port/in`/`port/out` dépasse 3-4 interfaces (ex : `port/in/device`, `port/in/zone`, `port/in/alarm`), SOLID, `final` systématique (paramètres, variables locales, champs — y compris `this.` obligatoire sur tout accès à un champ/méthode d'instance), records pour les value objects/événements/DTOs, suffixe systématique par rôle (`UseCase`/`Service`/`Repository`/`Document`/`Envelope`/... — voir tableau dans coding-standards.md), méthodes référence plutôt que lambdas triviales, JavaDoc obligatoire sur `domain`, pas de commentaires qui répètent le code. Lombok (`@RequiredArgsConstructor`, `@Getter`) pour le boilerplate DI/accesseurs, MapStruct (sans expression Java, méthodes nommées/communes via `uses=`) pour les traducteurs structurels entre couches, normalisation trim+lowercase des champs texte libre en couche présentation — voir coding-standards.md. Le module `cerbere-shared-kernel` (package `fr.cerbere.shared`) porte les objets et configurations communs (enveloppe Kafka, sérialiseur JSON, mapper/sécurité par défaut) — voir [ADR 0013](docs/adr/0013-module-cerbere-shared-kernel.md).

## Structure du repo

| Module | Rôle | Statut |
|---|---|---|
| `api-gateway` | Routage/sécurité (Spring Cloud Gateway) | Scaffold vide |
| `cerbere-bff` | Front interne (Thymeleaf/htmx/Beer CSS) + agrégation REST | En cours (tableau de bord alarme fait) |
| `cerbere-core` | Domaine alarme : zones, devices, arm/disarm, alertes | **Implémenté** |
| `cerbere-history` | Historique des événements/alertes | Scaffold vide |
| `cerbere-notification` | Envoi d'alertes (email/push) | Scaffold vide |
| `cerbere-devices-mock` | Simulation de devices (contact, mouvement, sirène) | **Implémenté** |
| `cerbere-shared-kernel` | Objets/configs communs (`EventEnvelope`, `JacksonEventSerializer`, `CommonJacksonConfig`, `PermitAllSecurityConfig`) | **Implémenté** |
| `deployment` | `docker-compose.yaml` (Mongo, Kafka, apps) | Mongo + Kafka (config KRaft corrigée) |
| `docs` | PRD, ADR, bonnes pratiques, architecture | À jour |
| `.github/workflows` | CI de publication GitHub Packages | `publish-packages.yml` (6 modules) |
| `README.md` | Vue d'entrée du projet (open source) | À jour |

## Lancer en local

```bash
# Infra (Mongo + Kafka)
cd deployment && docker compose up -d mongodb kafka

# cerbere-shared-kernel doit être installé en local avant tout module qui en dépend
# (pas de pom agrégateur racine, voir ADR 0009/0013)
cd cerbere-shared-kernel && ./mvnw install -DskipTests

# cerbere-devices-mock
cd cerbere-devices-mock && ./mvnw spring-boot:run
```

Les autres modules (hors `cerbere-shared-kernel`) ne sont pas encore implémentés (scaffolds vides).

## Journal des itérations

| Date | Itération | Livré | Références |
|---|---|---|---|
| 2026-07-22 | Fondations + devices-mock | Structure `docs/` complète (PRD, 9 ADR, best-practices, vue d'architecture), `CLAUDE.md` racine, module `cerbere-devices-mock` complet (clean architecture, simulation contact/mouvement/sirène, producteur Kafka, scheduler + API de contrôle), correction `deployment/docker-compose.yaml` (renommage + config Kafka KRaft) | [PRD 001](docs/prd/001-mvp-backend-alarme.md), ADR 0001-0009 |
| 2026-07-22 | Règles additionnelles | Module `contract` partagé autorisé (ADR 0010, supersede ADR 0005), suffixe systématique par rôle + `this.` obligatoire (coding-standards.md), renommage `PublishDeviceEventUseCase` → `PublishDeviceEventService`, config GitHub Packages sur les 6 modules + workflow CI (ADR 0011), `README.md` racine, correction du conflit de bean `ObjectMapper`/`JsonMapper` (`@Primary` retiré de `MapperConfig`), sérialiseur Kafka Jackson 3 écrit à la main (`DeviceEventEnvelopeSerializer`, ADR 0012) suite à l'incompatibilité du `JsonSerializer` Jackson 2 de spring-kafka. **Vérifié bout-en-bout** : device créé, événement déclenché, message confirmé sur `cerbere.device.events.raw` via `kafka-console-consumer` | ADR 0010, ADR 0011, ADR 0012 |
| 2026-07-22 | Module `cerbere-shared-kernel` | Module partagé créé (`fr.cerbere.shared`) : `EventEnvelope`/`JacksonEventSerializer` (généralisés depuis les équivalents `Device*` de `cerbere-devices-mock`), `CommonJacksonConfig`/`PermitAllSecurityConfig` (configs communes). `cerbere-devices-mock` migré pour en dépendre, fichiers dupliqués supprimés. Workflow CI corrigé : `chmod +x mvnw` (fix `Permission denied`) + job dédié publiant `cerbere-shared-kernel` avant les autres modules (`needs:`) | [ADR 0013](docs/adr/0013-module-cerbere-shared-kernel.md) |
| 2026-07-22 | `cerbere-core` complet | Domaine (Device/Zone/AlarmSystem avec `ArmingMode` AWAY/HOME sans état DISARMED représentable, CRUD complet device/zone), ports sous-packagés par agrégat, use-cases, persistence Mongo, producteurs/consommateur Kafka (`cerbere.alarm.state-changed`/`cerbere.alarm.alerts` provisionnés, `cerbere.device.events.raw` consommé), API REST (devices/zones/alarm). Ajout Lombok (`@RequiredArgsConstructor`/`@Getter`) et MapStruct (mappers structurels, méthodes nommées communes `CommonIdMapper`, pas d'expression Java) sur tout le module, normalisation trim+lowercase des champs texte libre en couche présentation (`CommonTextMapper`). Suppression du `compose.yaml` Initializr + dépendance `spring-boot-docker-compose` (redondant avec `deployment/docker-compose.yaml`, faisait planter le démarrage une fois le fichier supprimé). **Vérifié bout-en-bout** : zone/device créés avec normalisation confirmée, armement AWAY publié sur `cerbere.alarm.state-changed` | ADR 0014 |
| 2026-07-22 | Corrélation d'id device | `RegisterDeviceUseCase.register()` prend désormais l'id en entrée (celui du device réel/simulé côté bridge) au lieu d'en générer un, pour que `HandleDeviceEventService` puisse retrouver le device correspondant à un événement Kafka — conforme à l'intention déjà actée en ADR 0004. `Device.register`, le port, le service applicatif, `RegisterDeviceRequest` et `DeviceController` mis à jour en conséquence | ADR 0004 |
| 2026-07-22 | Front interne : tableau de bord alarme | Stack tranchée après recherche (ADR 0015) : Thymeleaf natif (fragments paramétrés, pas `thymeleaf-layout-dialect`) + htmx + Beer CSS. Scaffold `cerbere-bff` (Thymeleaf, RestClient vers `cerbere-core`, configs partagées importées), layout + nav, premier écran complet (statut alarme + armer AWAY/HOME + désarmer, swap htmx sans rechargement). DTOs `AlarmStatusResponse`/`ArmRequest` déplacés dans `cerbere-shared-kernel` (`fr.cerbere.shared.dto.alarm`) après duplication repérée entre `cerbere-core` et `cerbere-bff` — règle rappelée : tout DTO consommé par 2+ modules vit dans le shared-kernel. `htmx-spring-boot`/`htmx-spring-boot-thymeleaf` retirées : incompatibles Spring Boot 4.1 (`WebMvcRegistrations` déplacé), htmx utilisé en CDN brut à la place. **Vérifié dans le navigateur** : layout/nav/Beer CSS rendus, les 3 actions (armer absence/présence, désarmer) mettent à jour le badge de statut sans rechargement complet | ADR 0015 |

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
- [ADR 0012](docs/adr/0012-serializer-kafka-jackson3-maison.md) — Sérialiseur Kafka Jackson 3 écrit à la main (Accepted)
- [ADR 0013](docs/adr/0013-module-cerbere-shared-kernel.md) — Module `cerbere-shared-kernel` (Accepted)
- [ADR 0014](docs/adr/0014-provisionnement-explicite-topics-kafka.md) — Provisionnement explicite des topics Kafka (Accepted)
- [ADR 0015](docs/adr/0015-stack-front-interne-thymeleaf-htmx-beercss.md) — Stack front interne Thymeleaf/htmx/Beer CSS (Accepted)

## Dette technique connue / TODO

- Keycloak non branché : tous les services actuellement en sécurité `permitAll` avec `TODO Keycloak` explicite — ne pas exposer sur internet en l'état.
- `cerbere-core`, `cerbere-history`, `cerbere-notification`, `cerbere-bff`, `api-gateway` restent des scaffolds vides — prochaine itération.
- `deployment/docker-compose.yaml` ne contient encore que Mongo + Kafka + `cerbere-devices-mock` — les 5 autres services applicatifs et le conteneur SMTP factice (maildev) seront ajoutés avec `cerbere-notification`.
- Pas de tests d'intégration Kafka avec un vrai broker (embedded/Testcontainers) pour l'instant — tests unitaires sur les use-cases uniquement (la publication réelle a été vérifiée manuellement, pas encore automatisée).
- `spring-kafka` 3.3.8 n'a pas de sérialiseur JSON natif Jackson 3 : contournement centralisé dans `fr.cerbere.shared.kafka.JacksonEventSerializer` (ADR 0012/0013), réutilisable tel quel par les futurs producteurs.
- Pas de pom agrégateur racine réel (ADR 0009 le laissait optionnel) : builder `cerbere-shared-kernel` avant tout module qui en dépend reste une étape manuelle (`mvnw install`) tant qu'il n'est pas créé.
- `cerbere-core` a supprimé la dépendance `spring-boot-docker-compose` (et son `compose.yaml`) : à vérifier/nettoyer aussi sur les autres scaffolds (`api-gateway`, `cerbere-bff`, `cerbere-history`, `cerbere-notification`) quand ils seront implémentés, pour éviter le même conflit avec `deployment/docker-compose.yaml`.
- **Prochaine tâche notée par l'utilisateur** : `cerbere-devices-mock` sera un jour remplacé par un vrai microservice "bridge" pilotant les devices physiques réels. Ce bridge devra exposer/assigner l'id du device (celui transporté dans les événements Kafka) de façon **transparente pour l'utilisateur final via l'IHM** — pas de copier-coller manuel d'UUID attendu à terme, contrairement au flux actuel (BO admin) qui demande l'id explicitement à l'enregistrement d'un device (voir ADR 0004, corrigé le 2026-07-22).

## Règles de mise à jour pour l'IA

- À chaque itération : ajouter une ligne au **Journal des itérations** ci-dessus (date, ce qui a été livré, liens PRD/ADR).
- Toute décision d'architecture structurante (nouveau choix technique, nouveau pattern, changement de convention) → créer un nouvel ADR dans `docs/adr/`, le référencer dans ce fichier.
- Mettre à jour le tableau **Structure du repo** dès qu'un module change de statut (scaffold → implémenté).
- Mettre à jour `docs/architecture/vue-ensemble.md` si le schéma de communication entre services change.
- Ne jamais laisser ce fichier décrire du code qui n'existe pas encore comme s'il existait — utiliser "À venir"/"Scaffold vide" explicitement.
