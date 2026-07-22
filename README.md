# Cerbère

Système d'alarme domestique open source, construit en microservices Java/Spring Boot.

## Vision

Piloter un système d'alarme maison : armer/désarmer, superviser l'état des zones et devices, recevoir des alertes. Deux interfaces sont prévues à terme : un back-office d'administration (réseau interne) et une application internet (arm/disarm, état, alertes). Voir [docs/prd/000-vision-produit.md](docs/prd/000-vision-produit.md).

## État actuel

Cette itération pose les fondations : documentation (PRD/ADR/bonnes pratiques) et le module `cerbere-devices-mock`, qui simule des capteurs (contact porte/fenêtre, détecteur de mouvement) et un actionneur (sirène) en publiant des événements Kafka. Les autres microservices sont encore des scaffolds vides. Détail à jour dans [CLAUDE.md](CLAUDE.md).

## Architecture

```
api-gateway → cerbere-bff → { cerbere-core, cerbere-history }   (REST synchrone)
cerbere-devices-mock → Kafka → { cerbere-core, cerbere-history }
cerbere-core → Kafka → { cerbere-history, cerbere-notification }
```

Schéma complet : [docs/architecture/vue-ensemble.md](docs/architecture/vue-ensemble.md).

| Module | Rôle | Statut |
|---|---|---|
| `api-gateway` | Routage/sécurité (Spring Cloud Gateway) | Scaffold |
| `cerbere-bff` | Agrégation REST pour les interfaces front | Scaffold |
| `cerbere-core` | Zones, devices, arm/disarm, alertes | Scaffold |
| `cerbere-history` | Historique des événements/alertes | Scaffold |
| `cerbere-notification` | Envoi d'alertes (email/push) | Scaffold |
| `cerbere-devices-mock` | Simulation de devices | **Implémenté** |

## Stack technique

- Java 25, Spring Boot 4.1.0
- MongoDB (une base logique par service)
- Kafka (messaging inter-microservices)
- Clean architecture stricte par module, SOLID, Java immuable (`final` systématique, records)

Détail des conventions : [docs/best-practices/](docs/best-practices/).

## Démarrer en local

```bash
# Infrastructure (MongoDB + Kafka)
cd deployment && docker compose up -d mongodb kafka

# cerbere-devices-mock (seul module implémenté pour l'instant)
cd cerbere-devices-mock && ./mvnw spring-boot:run
```

L'API de `cerbere-devices-mock` écoute sur `http://localhost:8081` :
- `POST /api/devices-mock` — enregistrer un device simulé
- `GET /api/devices-mock` — lister les devices simulés
- `POST /api/devices-mock/{deviceId}/events` — déclencher manuellement un événement

## Documentation

- [PRD](docs/prd/) — exigences produit par itération
- [ADR](docs/adr/) — décisions d'architecture
- [Bonnes pratiques](docs/best-practices/) — conventions de code et d'architecture
- [CLAUDE.md](CLAUDE.md) — journal des itérations et contexte pour l'assistant IA

## Publication des packages

Chaque module est publié sur GitHub Packages via le workflow `.github/workflows/publish-packages.yml` à chaque push sur `main`. Voir [ADR 0011](docs/adr/0011-publication-github-packages.md).

## Licence

Projet open source — licence à définir.
