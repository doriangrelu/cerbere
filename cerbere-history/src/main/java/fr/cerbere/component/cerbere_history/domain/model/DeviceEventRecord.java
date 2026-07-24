package fr.cerbere.component.cerbere_history.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Historisation d'un événement brut de device (contact/mouvement/sirène),
 * reçu via {@code cerbere.device.events.raw}. {@code payload} varie selon le
 * type de device — voir {@code DeviceEventReport} côté {@code cerbere-core},
 * même principe. {@code eventId} sert d'identifiant de déduplication : un
 * message rejoué écrase le même enregistrement plutôt que d'en créer un autre.
 */
public record DeviceEventRecord(
	UUID eventId,
	UUID deviceId,
	UUID zoneId,
	String eventType,
	Map<String, Object> payload,
	Instant occurredAt,
	Instant recordedAt,
	UUID correlationId
) {
}
