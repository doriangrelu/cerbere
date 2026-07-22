package fr.cerbere.shared.dto.devicemock;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Requête d'enregistrement d'un nouveau device simulé. {@code type} doit
 * correspondre à une constante de {@code DeviceType} (CONTACT, MOTION, SIREN).
 * Contrat REST partagé entre {@code cerbere-bff} (émetteur) et
 * {@code cerbere-devices-mock} (récepteur).
 */
public record RegisterSimulatedDeviceRequest(
	@NotNull String type,
	@NotBlank String label,
	String zoneId,
	boolean autoSimulate
) {
}
