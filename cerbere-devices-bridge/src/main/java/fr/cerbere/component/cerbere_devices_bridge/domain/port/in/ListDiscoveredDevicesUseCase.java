package fr.cerbere.component.cerbere_devices_bridge.domain.port.in;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.DiscoveredDevice;

import java.util.List;

/**
 * Port d'entrée : lister les devices vus sur MQTT en attente d'appairage
 * (voir ADR 0023).
 */
public interface ListDiscoveredDevicesUseCase {

	List<DiscoveredDevice> listAll();
}
