package fr.cerbere.component.cerbere_core.domain.port.out.device;

import fr.cerbere.component.cerbere_core.domain.model.Device;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie : persistance du registre officiel des devices.
 */
public interface DeviceRepository {

	Device save(Device device);

	Optional<Device> findById(UUID id);

	List<Device> findAll();

	List<Device> findByZoneId(UUID zoneId);

	Optional<Device> findByLabel(String label);

	void deleteById(UUID id);
}
