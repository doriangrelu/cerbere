package fr.cerbere.component.cerbere_devices_bridge.domain.port.out;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.BridgedDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie : persistance du miroir local des devices physiques.
 */
public interface BridgedDeviceRepository {

	BridgedDevice save(BridgedDevice device);

	Optional<BridgedDevice> findById(UUID id);

	List<BridgedDevice> findAll();

	List<BridgedDevice> findByType(DeviceType type);

	void deleteById(UUID id);
}
