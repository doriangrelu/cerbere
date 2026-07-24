package fr.cerbere.component.cerbere_devices_bridge.domain.exception;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceState;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;

import java.util.UUID;

/**
 * Levée lorsqu'un état rapporté n'appartient pas à la famille d'états supportée
 * par le type du device (signale un bug de traduction payload → domaine).
 */
public final class UnsupportedDeviceStateException extends RuntimeException {

	public UnsupportedDeviceStateException(final UUID deviceId, final DeviceType deviceType, final DeviceState reportedState) {
		super("Device " + deviceId + " of type " + deviceType + " does not support state " + reportedState);
	}
}
