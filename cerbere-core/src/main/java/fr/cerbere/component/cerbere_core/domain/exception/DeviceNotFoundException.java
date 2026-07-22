package fr.cerbere.component.cerbere_core.domain.exception;

import java.util.UUID;

/**
 * Levée lorsqu'un device du registre officiel demandé n'existe pas.
 */
public final class DeviceNotFoundException extends RuntimeException {

	public DeviceNotFoundException(final UUID deviceId) {
		super("Device not found: " + deviceId);
	}
}
