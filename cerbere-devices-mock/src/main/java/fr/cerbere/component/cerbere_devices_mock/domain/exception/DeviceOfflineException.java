package fr.cerbere.component.cerbere_devices_mock.domain.exception;

import java.util.UUID;

/**
 * Levée lorsqu'on tente de faire émettre un device marqué hors réseau : un
 * device injoignable ne publie rien, pas même sur commande manuelle — c'est
 * précisément ce qu'on simule (voir ADR 0024).
 */
public final class DeviceOfflineException extends RuntimeException {

	public DeviceOfflineException(final UUID deviceId) {
		super("Simulated device is offline and cannot emit: " + deviceId);
	}
}
