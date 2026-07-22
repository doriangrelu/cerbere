package fr.cerbere.component.cerbere_devices_mock.domain.exception;

import java.util.UUID;

/**
 * Levée lorsqu'un device simulé demandé n'existe pas dans le registre de simulation.
 */
public final class DeviceNotFoundException extends RuntimeException {

	public DeviceNotFoundException(final UUID deviceId) {
		super("Simulated device not found: " + deviceId);
	}
}
