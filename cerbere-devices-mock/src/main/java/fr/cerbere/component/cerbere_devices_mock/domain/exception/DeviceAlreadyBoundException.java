package fr.cerbere.component.cerbere_devices_mock.domain.exception;

import java.util.UUID;

/**
 * Levée quand une tentative de liaison cible un device du registre officiel
 * déjà lié à un autre miroir local (id déjà connu de {@code cerbere-devices-mock}).
 */
public final class DeviceAlreadyBoundException extends RuntimeException {

	public DeviceAlreadyBoundException(final UUID coreDeviceId) {
		super("Device already bound to a simulated device: " + coreDeviceId);
	}
}
