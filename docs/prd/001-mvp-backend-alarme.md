# PRD 001 — MVP Backend Alarme

## Historique des révisions

| Date | Itération | Changement |
|---|---|---|
| 2026-07-22 | Fondations + cerbere-devices-mock | Création initiale. Périmètre de cette itération : docs/ADR/CLAUDE.md + module `cerbere-devices-mock`. |

## Contexte

Suite à la vision produit ([000-vision-produit.md](000-vision-produit.md)), ce PRD cadre le premier incrément livrable : un backend microservices fonctionnel pour le système d'alarme, sans interface web, avec une couche devices mockée.

## Objectifs de ce MVP

- Disposer d'une architecture microservices posée (clean architecture, Kafka, MongoDB) qui serve de socle à toutes les itérations suivantes.
- Simuler des capteurs (contact porte/fenêtre, détecteur de mouvement) et un actionneur (sirène) de façon suffisamment réaliste pour que les futurs services métier (`cerbere-core`, `cerbere-history`, `cerbere-notification`) puissent consommer des événements sans attendre du vrai matériel.
- Documenter chaque décision d'architecture structurante via ADR, pour garder une trace des choix et de leurs alternatives écartées.

## Périmètre (in)

- Documentation : PRD, ADR initiaux, bonnes pratiques, vue d'architecture, `CLAUDE.md` racine.
- Module `cerbere-devices-mock` : simulation de devices (scheduler + API de contrôle), publication d'événements Kafka sur `cerbere.device.events.raw`.

## Périmètre (out, itérations suivantes)

- `cerbere-core` (registre officiel des devices, zones, état de l'alarme, arm/disarm, évaluation des alertes).
- `cerbere-history` (persistance de l'historique consultable).
- `cerbere-notification` (envoi des alertes par email/push).
- `cerbere-bff` et `api-gateway` (agrégation et routage).
- `deployment/docker-compose.yaml` complet (tous les services applicatifs).
- Authentification Keycloak.
- Interfaces web.

## User stories (pour cette itération)

1. **En tant que** service consommateur (futur `cerbere-core`), **je veux** recevoir des événements de devices au format standardisé sur Kafka, **afin de** pouvoir évaluer l'état de l'alarme sans dépendre de vrai matériel.
2. **En tant que** développeur/testeur, **je veux** déclencher manuellement un événement précis sur un device simulé via une API REST, **afin de** pouvoir démontrer/tester un scénario (ex : ouverture de porte) à la demande.
3. **En tant qu**'opérateur, **je veux** que des devices simulés génèrent des événements automatiquement (scheduler), **afin d'**avoir un flux de données continu sans action manuelle pour les démonstrations et le développement.

## Critères d'acceptation

- Un device simulé (contact, mouvement ou sirène) peut être créé via l'API REST du module `cerbere-devices-mock`.
- Un événement peut être déclenché manuellement sur un device via `POST /api/devices-mock/{deviceId}/events` et apparaît sur le topic Kafka `cerbere.device.events.raw` avec l'enveloppe d'événement standard (voir [ADR 0002](../adr/0002-enveloppe-evenement-et-topics-kafka.md)).
- Un device marqué en simulation automatique génère des événements périodiquement sans intervention manuelle.
- Le code respecte la structure clean architecture définie dans [docs/best-practices/clean-architecture.md](../best-practices/clean-architecture.md) et les conventions de [docs/best-practices/coding-standards.md](../best-practices/coding-standards.md).
- MongoDB et Kafka démarrent correctement via `deployment/docker-compose.yaml`.

## Hors-scope (rappel)

Keycloak, front-end, vrais devices physiques, autres microservices métier — voir tableau ci-dessus.
