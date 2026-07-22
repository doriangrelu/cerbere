# Bonnes pratiques — Conventions Kafka

Référence technique détaillée pour [ADR 0002](../adr/0002-enveloppe-evenement-et-topics-kafka.md).

## Nommage des topics

`cerbere.<contexte>.<famille-événement>`, tout en minuscules, mots séparés par des tirets si besoin, segments séparés par des points.

| Topic | Producteur | Consommateurs | Clé de partition | Statut |
|---|---|---|---|---|
| `cerbere.device.events.raw` | `cerbere-devices-mock` | `cerbere-core`, `cerbere-history` | `deviceId` | Implémenté (producteur) |
| `cerbere.alarm.state-changed` | `cerbere-core` | `cerbere-history`, `cerbere-notification` | `zoneId` \| `systemId="home-1"` | À venir |
| `cerbere.alarm.alerts` | `cerbere-core` | `cerbere-history`, `cerbere-notification` | `zoneId` \| `systemId="home-1"` | À venir |

## Enveloppe d'événement

Tout message publié sur un topic Cerbère respecte cette structure JSON :

```json
{
  "eventId": "b3f1c2a0-...-uuid",
  "eventType": "device.contact.state_changed",
  "occurredAt": "2026-07-22T10:15:30Z",
  "producedAt": "2026-07-22T10:15:30.123Z",
  "source": "cerbere-devices-mock",
  "deviceId": "d1e2f3a4-...-uuid",
  "zoneId": "z1e2f3a4-...-uuid",
  "correlationId": "c1e2f3a4-...-uuid",
  "payload": { }
}
```

| Champ | Type | Obligatoire | Description |
|---|---|---|---|
| `eventId` | UUID string | oui | Identifiant unique de l'événement (idempotence côté consommateur) |
| `eventType` | string | oui | Type métier, format `<domaine>.<sous-type>.<action>` (ex: `device.contact.state_changed`, `device.motion.detected`, `device.siren.triggered`) |
| `occurredAt` | ISO-8601 UTC | oui | Instant métier de l'événement |
| `producedAt` | ISO-8601 UTC | oui | Instant de publication sur Kafka (peut différer si rejeu) |
| `source` | string | oui | Nom du service producteur (`spring.application.name`) |
| `deviceId` | UUID string \| null | non | Présent si l'événement concerne un device précis |
| `zoneId` | UUID string \| null | non | Présent si l'événement concerne une zone précise |
| `correlationId` | UUID string | oui | Permet de relier plusieurs événements issus d'une même chaîne causale |
| `payload` | objet JSON | oui (peut être vide) | Données spécifiques à `eventType`, propres à chaque famille d'événement |

## eventType — devices V1

- `device.contact.state_changed` — payload : `{ "state": "OPEN" | "CLOSED" }`
- `device.motion.detected` — payload : `{ "detected": true | false }`
- `device.siren.triggered` — payload : `{ "active": true | false }`

## Règles d'implémentation

- **Pas de librairie Java partagée** pour cette enveloppe ([ADR 0005](../adr/0005-pas-de-librairie-partagee-evenements.md)) : chaque service définit son propre `record` Jackson, en lecture tolérante (ignore les champs/`eventType` inconnus plutôt que de lever une erreur de désérialisation).
- Un producteur ne publie jamais un message sans `eventId`, `eventType`, `occurredAt`, `producedAt`, `source`, `correlationId` renseignés.
- Un consommateur doit ignorer silencieusement (avec un log niveau `debug`) tout `eventType` qu'il ne sait pas traiter — jamais faire échouer le listener sur un type inconnu.
- Un consumer group par service (nom = `spring.application.name`), jamais de consumer group partagé entre deux services différents.
