package fr.cerbere.shared.dto.device;

/**
 * Contrat REST partagé entre {@code cerbere-core} (producteur) et
 * {@code cerbere-bff} (consommateur) — voir ADR 0010/0013.
 */
public record DeviceResponse(
	String id,
	String type,
	String label,
	String zoneId,
	boolean enabled
) {
}
