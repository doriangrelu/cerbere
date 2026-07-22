package fr.cerbere.component.cerbere_devices_mock.adapter.in.web.dto;

import java.time.Instant;

/**
 * Représentation REST d'un événement de device publié.
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
