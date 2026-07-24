# Vue d'ensemble de l'architecture

Document vivant, à tenir à jour à chaque itération (voir règle de mise à jour dans `CLAUDE.md`).

## Schéma

```
                         Internet / réseau interne
                                    │  HTTPS
                                    ▼
                    ┌───────────────────────────────┐
                    │         api-gateway            │  (Spring Cloud Gateway MVC)
                    │  routes /api/public/**  (BFF)   │  ← phase 2: Keycloak
                    │  routes /api/admin/**   (core)  │
                    └───────────────┬────────────────┘
                                    │ REST sync (HTTP)
                                    ▼
                    ┌───────────────────────────────┐
                    │         cerbere-bff            │  (agrégateur, pas de DB)
                    └───────┬───────────────┬─────────┘
                     REST   │               │  REST
                            ▼               ▼
                 ┌─────────────────┐   ┌──────────────────┐
                 │   cerbere-core   │   │  cerbere-history  │
                 │ (état alarme,    │   │ (audit/lecture    │
                 │  zones, devices, │   │  historique)      │
                 │  arm/disarm)     │   └──────────────────┘
                 └───┬─────────▲───┘             ▲
                     │Kafka    │Kafka             │Kafka (consume tout)
                     │publish  │consume           │
                     ▼         │                  │
      ┌───────────────────────┴──────────────────┴───────────┐
      │                    Kafka (broker)                     │
      │  device.events.raw │ alarm.state-changed │ alarm.alerts│
      └───────────▲────────────────────────────────┬──────────┘
                  │ publish                         │ consume
      ┌───────────┴───────────┐         ┌───────────▼───────────────┐
      │ cerbere-devices-mock   │         │   cerbere-notification     │
      │ (simule capteurs/sirène)│        │ (email + push restclient)  │
      └────────────────────────┘         └────────────────────────────┘
```

## État d'implémentation par service

| Service | Statut | Rôle |
|---|---|---|
| `cerbere-devices-mock` | **Implémenté** | Simule contact porte/fenêtre, mouvement, sirène. Publie `cerbere.device.events.raw`, consomme `cerbere.device.state`. |
| `cerbere-core` | **Implémenté** | Registre devices/zones, état alarme, arm/disarm, évaluation des alertes |
| `cerbere-history` | **Implémenté (cette itération)** | Persistance et consultation paginée de l'historique des événements (device events, changements d'état alarme, alertes) |
| `cerbere-notification` | À venir | Envoi d'alertes (email + push) |
| `cerbere-bff` | **Implémenté** | Agrégation REST (alarme, devices, zones, mode test) — pas encore d'écran historique |
| `api-gateway` | À venir | Routage public/admin, sécurité |

## Principes de communication

- **Synchrone (REST)** : `api-gateway → cerbere-bff → {cerbere-core, cerbere-history}`. Jamais de REST synchrone entre `cerbere-core`/`cerbere-history`/`cerbere-notification` pour les flux d'événements — c'est le rôle de Kafka.
- **Asynchrone (Kafka)** : tout ce qui est événementiel — devices → core/history, changements d'état d'alarme/alertes → history/notification. Voir [docs/best-practices/kafka-conventions.md](../best-practices/kafka-conventions.md) pour le détail des topics et de l'enveloppe.
- **Persistance** : MongoDB, un serveur, une base logique par service, jamais d'accès direct à la base d'un autre service (voir [ADR 0003](../adr/0003-mongodb-database-per-service.md)).

## Références

- Architecture par module : [docs/best-practices/clean-architecture.md](../best-practices/clean-architecture.md)
- Décisions structurantes : [docs/adr/](../adr/)
- Exigences produit : [docs/prd/](../prd/)
