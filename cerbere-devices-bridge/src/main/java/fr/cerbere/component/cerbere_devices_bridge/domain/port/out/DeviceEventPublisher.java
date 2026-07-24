package fr.cerbere.component.cerbere_devices_bridge.domain.port.out;

import fr.cerbere.component.cerbere_devices_bridge.domain.event.DeviceEventOccurred;

/**
 * Port de sortie : publication d'un événement de device vers le monde extérieur (Kafka).
 */
public interface DeviceEventPublisher {

	void publish(DeviceEventOccurred event);
}
