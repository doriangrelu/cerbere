package fr.cerbere.component.cerbere_core.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Signal d'entrée représentant un événement de device rapporté depuis
 * l'extérieur (Kafka, via {@code cerbere.device.events.raw}). Volontairement
 * découplé de l'enveloppe Kafka partagée ({@code fr.cerbere.shared.event.EventEnvelope})
 * : le domaine ne connaît que ce dont il a besoin pour évaluer une alerte,
 * la traduction depuis l'enveloppe est un souci d'infrastructure.
 */
public record DeviceEventReport(
	UUID deviceId,
	String eventType,
	Map<String, Object> payload,
	Instant occurredAt,
	UUID correlationId
) {
}
