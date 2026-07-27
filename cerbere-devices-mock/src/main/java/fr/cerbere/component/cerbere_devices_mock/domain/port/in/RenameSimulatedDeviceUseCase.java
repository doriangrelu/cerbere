package fr.cerbere.component.cerbere_devices_mock.domain.port.in;

import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;

import java.util.UUID;

/**
 * Port d'entrée : renommer le {@code friendlyName} MQTT d'un device simulé —
 * action d'appairage à un device du registre officiel (voir ADR 0021), symétrique
 * au renommage manuel du {@code friendly_name} Zigbee2MQTT pour du matériel réel.
 */
public interface RenameSimulatedDeviceUseCase {

	SimulatedDevice rename(UUID id, String newFriendlyName);
}
