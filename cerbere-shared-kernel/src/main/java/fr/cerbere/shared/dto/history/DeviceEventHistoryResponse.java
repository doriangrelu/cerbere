package fr.cerbere.shared.dto.history;

import java.time.Instant;
import java.util.Map;

/**
 * Contrat REST partagé entre {@code cerbere-history} (producteur) et
 * {@code cerbere-bff} (consommateur) — voir ADR 0010/0013. Reflète un
 * événement brut de device (contact/mouvement/sirène) historisé depuis
 * {@code cerbere.device.events.raw}. {@code payload} varie selon le type de
 * device, comme côté {@code cerbere-core}.
 */
public record DeviceEventHistoryResponse(
	String eventId,
	String deviceId,
	String zoneId,
	String eventType,
	Map<String, Object> payload,
	Instant occurredAt,
	Instant recordedAt,
	String correlationId
) {
}
