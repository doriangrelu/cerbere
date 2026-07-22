package fr.cerbere.component.cerbere_devices_mock.domain.port.out;

import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie : persistance des devices simulés.
 */
public interface SimulatedDeviceRepository {

	SimulatedDevice save(SimulatedDevice device);

	Optional<SimulatedDevice> findById(UUID id);

	List<SimulatedDevice> findAll();

	List<SimulatedDevice> findByAutoSimulateTrue();
}
