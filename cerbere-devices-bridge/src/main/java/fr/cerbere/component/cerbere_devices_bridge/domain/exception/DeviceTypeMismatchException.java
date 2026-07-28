package fr.cerbere.component.cerbere_devices_bridge.domain.exception;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;

import java.util.UUID;

/**
 * Levée quand une demande d'appairage associe un device physique à un device du
 * registre officiel d'un autre type — un détecteur de mouvement branché sur une
 * entrée déclarée comme contact, par exemple. Les payloads seraient alors
 * interprétés selon le mauvais contrat, et l'alarme évaluerait des violations
 * qui n'ont pas de sens (voir ADR 0023).
 */
public final class DeviceTypeMismatchException extends RuntimeException {

	public DeviceTypeMismatchException(final String friendlyName, final DeviceType discoveredType,
										final UUID coreDeviceId, final DeviceType coreDeviceType) {
		super("Device " + friendlyName + " looks like a " + discoveredType
			+ " but device " + coreDeviceId + " is declared as " + coreDeviceType);
	}
}
