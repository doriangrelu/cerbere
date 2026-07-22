# PRD 000 — Vision produit : Cerbère

## Historique des révisions

| Date | Itération | Changement |
|---|---|---|
| 2026-07-22 | Fondations | Création initiale |

## Contexte

Cerbère est un système d'alarme domestique piloté par logiciel, développé pour un usage personnel (une seule maison). L'objectif est de remplacer/compléter un système d'alarme physique classique par une plateforme logicielle capable de piloter des capteurs et actionneurs, d'exposer l'état du système, et d'alerter en cas d'incident.

## Objectifs produit

- Piloter l'armement/désarmement du système d'alarme.
- Superviser l'état courant du système et de ses zones/devices.
- Remonter des alertes en cas d'événement suspect pendant que le système est armé.
- Fournir deux interfaces distinctes selon le contexte d'accès :
  - **Interface réseau interne** : back-office d'administration (gestion des devices, zones, utilisateurs, configuration).
  - **Interface internet** : armer/désarmer, consulter l'état, recevoir les alertes — usage nomade.

## Contraintes non-négociables

- **MongoDB** comme unique système de persistance (souplesse de schéma).
- **Kafka** comme message broker pour toute communication asynchrone inter-microservices.
- **Clean architecture** stricte dans chaque module (séparation domain/application/infrastructure/adapter).
- **Java moderne et immuable** : streams, lambdas, `final` systématique sur arguments/propriétés/variables locales.
- **SOLID** appliqué strictement.
- Nommage clair, JavaDoc sur le code, documentation technique tenue à jour.
- Gouvernance documentaire vivante : PRD, ADR, bonnes pratiques, `CLAUDE.md` auto-maintenu par l'assistant IA au fil des itérations.
- Déploiement complet via `deployment/docker-compose.yaml`.
- Authentification via **Keycloak** — prévue en phase 2, non implémentée dans le MVP.
- La couche **devices** est mockée dans un premier temps (pas de vrai matériel), de façon à ce que les microservices métier consomment des données réalistes avant le branchement de capteurs physiques.

## Périmètre par phase

| Phase | Contenu |
|---|---|
| **Phase 1 (en cours)** | Backend complet (microservices, Kafka, MongoDB), devices mockés, pas d'interface web |
| **Phase 2** | Authentification Keycloak, synchronisation utilisateurs |
| **Phase 3** | Interfaces web (BO admin réseau interne, appli internet), branchement de vrais devices |

## Hors-scope (pour l'instant)

- Toute interface graphique (web/mobile).
- Intégration avec du matériel physique réel.
- Multi-site / multi-tenant (le système est conçu pour une seule maison).
- Authentification/autorisation avancée (en attendant Keycloak).

Voir [001-mvp-backend-alarme.md](001-mvp-backend-alarme.md) pour le détail du MVP backend en cours de construction.
