package fr.cerbere.shared.dto.devicemock;

import java.time.Instant;

/**
 * Représentation REST d'un événement de device publié. Contrat partagé entre
 * {@code cerbere-devices-mock} (producteur) et {@code cerbere-bff}
 * (consommateur, section "mode test").
 */
public record DeviceEventResponse(
	String eventId,
	String deviceId,
	String zoneId,
	String deviceType,
	String newState,
	Instant occurredAt,
	String correlationId,
	boolean triggeredManually
) {
}
