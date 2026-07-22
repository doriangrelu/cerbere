package fr.cerbere.shared.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Enveloppe d'événement commune à tout Cerbère (voir ADR 0002 et
 * docs/best-practices/kafka-conventions.md). Portée par le module partagé
 * {@code cerbere-shared-kernel} (voir ADR 0010, ADR 0013) : tout service qui
 * publie ou consomme un événement Kafka Cerbère dépend de ce même type,
 * plutôt que de redéfinir sa propre copie.
 */
public record EventEnvelope(
	UUID eventId,
	String eventType,
	Instant occurredAt,
	Instant producedAt,
	String source,
	UUID deviceId,
	UUID zoneId,
	UUID correlationId,
	Map<String, Object> payload
) {
}
