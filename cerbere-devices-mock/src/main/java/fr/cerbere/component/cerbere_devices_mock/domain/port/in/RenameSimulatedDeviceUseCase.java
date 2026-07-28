package fr.cerbere.component.cerbere_devices_mock.domain.port.in;

import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;

/**
 * Port d'entrée : renommer le {@code friendlyName} MQTT d'un device simulé —
 * c'est le geste d'appairage, déclenché par {@code cerbere-devices-bridge} via
 * l'API bridge de Zigbee2MQTT dont le Mock joue le rôle (voir ADR 0021/0023).
 * Le device est désigné par son {@code friendlyName} courant, comme le fait la
 * vraie passerelle Zigbee2MQTT — jamais par l'id interne du Mock, qui n'existe
 * pas côté MQTT.
 */
public interface RenameSimulatedDeviceUseCase {

	SimulatedDevice rename(String currentFriendlyName, String newFriendlyName);
}
