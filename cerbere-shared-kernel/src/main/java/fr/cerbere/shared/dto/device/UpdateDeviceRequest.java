package fr.cerbere.shared.dto.device;

import jakarta.validation.constraints.NotBlank;

/**
 * Contrat REST partagé entre {@code cerbere-bff} (émetteur) et
 * {@code cerbere-core} (récepteur, {@code PUT /api/devices/{id}}).
 */
public record UpdateDeviceRequest(
	@NotBlank String label,
	String zoneId,
	boolean enabled
) {
}
