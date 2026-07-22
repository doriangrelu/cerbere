package fr.cerbere.component.cerbere_core.adapter.in.web.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * {@code id} est l'identifiant du device réel/simulé côté bridge (celui
 * transporté dans les événements Kafka), pas généré par le serveur — voir
 * ADR 0004.
 */
public record RegisterDeviceRequest(
	@NotBlank String id,
	@NotNull String type,
	@NotBlank String label,
	String zoneId
) {
}
