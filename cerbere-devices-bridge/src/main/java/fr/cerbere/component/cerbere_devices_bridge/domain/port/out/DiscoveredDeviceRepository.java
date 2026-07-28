package fr.cerbere.component.cerbere_devices_bridge.domain.port.out;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.DiscoveredDevice;

import java.util.List;
import java.util.Optional;

/**
 * Port de sortie : persistance des devices découverts sur MQTT mais pas encore
 * appairés à un device du registre officiel (voir ADR 0023).
 */
public interface DiscoveredDeviceRepository {

	DiscoveredDevice save(DiscoveredDevice device);

	Optional<DiscoveredDevice> findByFriendlyName(String friendlyName);

	List<DiscoveredDevice> findAll();

	void deleteByFriendlyName(String friendlyName);
}
