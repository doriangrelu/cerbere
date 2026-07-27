package fr.cerbere.component.cerbere_devices_mock.domain.port.out;

import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;

/**
 * Port de sortie : publication de l'état d'un device simulé vers MQTT, avec
 * exactement les mêmes payloads qu'un vrai device Zigbee2MQTT (voir ADR 0021,
 * docs/architecture/mqtt-zigbee-contracts.md) — le Mock se fait passer pour du
 * matériel réel afin de permettre de tester {@code cerbere-devices-bridge} de
 * bout en bout.
 */
public interface DeviceStatePublisher {

	void publish(String friendlyName, DeviceType type, DeviceState state);
}
