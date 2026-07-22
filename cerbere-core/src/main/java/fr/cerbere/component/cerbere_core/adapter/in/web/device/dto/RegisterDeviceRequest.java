package fr.cerbere.component.cerbere_core.adapter.in.web.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDeviceRequest(
	@NotNull String type,
	@NotBlank String label,
	String zoneId
) {
}
