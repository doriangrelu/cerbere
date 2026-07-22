package fr.cerbere.component.cerbere_devices_mock.domain.port.in;

import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;

import java.util.List;

/**
 * Port d'entrée : lister les devices simulés existants.
 */
public interface ListSimulatedDevicesUseCase {

	List<SimulatedDevice> listAll();
}
