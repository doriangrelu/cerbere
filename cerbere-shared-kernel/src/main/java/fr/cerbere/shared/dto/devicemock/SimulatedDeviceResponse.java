package fr.cerbere.shared.dto.devicemock;

/**
 * Contrat REST partagé entre {@code cerbere-devices-mock} (producteur) et
 * {@code cerbere-bff} (consommateur, section "mode test") — voir ADR 0010/0013.
 */
public record SimulatedDeviceResponse(
	String id,
	String type,
	String label,
	String zoneId,
	boolean autoSimulate,
	String currentState
) {
}
