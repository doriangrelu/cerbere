package fr.cerbere.component.cerbere_devices_mock.domain.exception;

import java.util.UUID;

/**
 * Levée lorsqu'un device simulé demandé n'existe pas dans le registre de
 * simulation, qu'il soit désigné par son id interne (API de contrôle) ou par son
 * {@code friendlyName} MQTT (requête de renommage venue du Bridge, voir ADR 0023).
 */
public final class DeviceNotFoundException extends RuntimeException {

	public DeviceNotFoundException(final UUID deviceId) {
		super("Simulated device not found: " + deviceId);
	}

	public DeviceNotFoundException(final String friendlyName) {
		super("Simulated device not found for friendly name: " + friendlyName);
	}
}
