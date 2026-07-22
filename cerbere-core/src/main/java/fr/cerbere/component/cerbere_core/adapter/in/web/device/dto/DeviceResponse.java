package fr.cerbere.component.cerbere_core.adapter.in.web.device.dto;

public record DeviceResponse(
	String id,
	String type,
	String label,
	String zoneId,
	boolean enabled
) {
}
