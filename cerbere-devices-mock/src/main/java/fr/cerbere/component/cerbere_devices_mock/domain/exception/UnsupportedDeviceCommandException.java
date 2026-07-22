package fr.cerbere.component.cerbere_devices_mock.domain.exception;

import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;

import java.util.UUID;

/**
 * Levée lorsqu'un état demandé n'appartient pas à la famille d'états supportée
 * par le type du device (ex: demander un état de sirène sur un contact porte/fenêtre).
 */
public final class UnsupportedDeviceCommandException extends RuntimeException {

	public UnsupportedDeviceCommandException(final UUID deviceId, final DeviceType deviceType, final DeviceState requestedState) {
		super("Device " + deviceId + " of type " + deviceType + " does not support state " + requestedState);
	}
}
