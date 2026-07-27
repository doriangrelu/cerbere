package fr.cerbere.shared.dto.devicemock;

import java.time.Instant;

/**
 * Représentation REST d'un état de device simulé publié (via MQTT — voir
 * ADR 0021). Contrat partagé entre {@code cerbere-devices-mock} (producteur)
 * et {@code cerbere-bff} (consommateur, section "mode test").
 */
public record DeviceEventResponse(
	String eventId,
	String deviceId,
	String deviceType,
	String newState,
	Instant occurredAt,
	boolean triggeredManually
) {
}
