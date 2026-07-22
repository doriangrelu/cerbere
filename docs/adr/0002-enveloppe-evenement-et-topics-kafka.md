# ADR 0002 — Enveloppe d'événement commune et conventions de topics Kafka

## Statut

Accepted

## Contexte

Kafka est le message broker retenu pour toute communication asynchrone inter-microservices (devices → core/history, changements d'état d'alarme → history/notification). Sans convention commune, chaque service risque d'inventer son propre format d'événement, rendant l'écosystème incohérent et difficile à faire évoluer.

## Décision

- **Nommage des topics** : `cerbere.<contexte>.<famille-événement>`, en minuscules, séparés par des points. Topics de cette itération : `cerbere.device.events.raw` (devices simulés → core/history, à venir), `cerbere.alarm.state-changed` et `cerbere.alarm.alerts` (core → history/notification, à venir).
- **Clé de partition** : `deviceId` pour les événements device, `zoneId` (ou `systemId="home-1"` par défaut, système mono-site) pour les événements d'alarme.
- **Un consumer group par service** sur les topics qu'il consomme (`cerbere-core`, `cerbere-history`, `cerbere-notification`), pour garantir que chaque service reçoit sa propre copie de chaque message (fan-out), sans compétition entre services.
- **Enveloppe commune** pour tous les événements du système :

```json
{
  "eventId": "uuid",
  "eventType": "device.contact.state_changed",
  "occurredAt": "2026-07-22T10:15:30Z",
  "producedAt": "2026-07-22T10:15:30.123Z",
  "source": "cerbere-devices-mock",
  "deviceId": "uuid|null",
  "zoneId": "uuid|null",
  "correlationId": "uuid",
  "payload": { }
}
```

`occurredAt` = instant métier de l'événement, `producedAt` = instant de publication (utile en cas de rejeu/retard). `payload` porte les données spécifiques à `eventType`.

## Conséquences

- Tout nouveau service qui consomme Kafka sait à quoi s'attendre sans devoir lire le code du producteur.
- Le détail du contrat (schéma JSON, exemples) est documenté dans [docs/best-practices/kafka-conventions.md](../best-practices/kafka-conventions.md), référence commune à tous les services.
- Voir [ADR 0005](0005-pas-de-librairie-partagee-evenements.md) pour la décision de ne pas partager de code Java autour de cette enveloppe.
