package fr.cerbere.component.cerbere_devices_bridge.domain.exception;

import java.util.UUID;

/**
 * Levée lorsqu'un device physique demandé n'existe pas dans le miroir local du bridge.
 */
public final class DeviceNotFoundException extends RuntimeException {

	public DeviceNotFoundException(final UUID deviceId) {
		super("Bridged device not found: " + deviceId);
	}
}
