package fr.cerbere.component.cerbere_core.adapter.in.web.device.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateDeviceRequest(
	@NotBlank String label,
	String zoneId,
	boolean enabled
) {
}
