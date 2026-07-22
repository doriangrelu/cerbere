package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.ListSimulatedDevicesUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;

import java.util.List;

/**
 * Implémentation du use-case de consultation des devices simulés.
 */
public final class ListSimulatedDevicesService implements ListSimulatedDevicesUseCase {

	private final SimulatedDeviceRepository simulatedDeviceRepository;

	public ListSimulatedDevicesService(final SimulatedDeviceRepository simulatedDeviceRepository) {
		this.simulatedDeviceRepository = simulatedDeviceRepository;
	}

	@Override
	public List<SimulatedDevice> listAll() {
		return this.simulatedDeviceRepository.findAll();
	}
}
