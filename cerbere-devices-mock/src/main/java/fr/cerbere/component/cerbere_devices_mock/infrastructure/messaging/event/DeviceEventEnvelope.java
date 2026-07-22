package fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Enveloppe d'événement Kafka commune à tout Cerbère (voir ADR 0002 et
 * docs/best-practices/kafka-conventions.md). Ce record est propre à ce module
 * ({@code cerbere-devices-mock}), volontairement non partagé via une librairie
 * commune (voir ADR 0005).
 */
public record DeviceEventEnvelope(
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
