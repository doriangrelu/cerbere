package fr.cerbere.component.cerbere_devices_mock.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Requête d'enregistrement d'un nouveau device simulé. {@code type} doit correspondre
 * à une constante de {@code DeviceType} (CONTACT, MOTION, SIREN).
 */
public record RegisterSimulatedDeviceRequest(
	@NotNull String type,
	@NotBlank String label,
	String zoneId,
	boolean autoSimulate
) {
}
