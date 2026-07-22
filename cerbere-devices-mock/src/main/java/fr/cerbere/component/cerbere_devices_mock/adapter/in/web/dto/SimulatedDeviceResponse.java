package fr.cerbere.component.cerbere_devices_mock.adapter.in.web.dto;

/**
 * Représentation REST d'un device simulé.
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
