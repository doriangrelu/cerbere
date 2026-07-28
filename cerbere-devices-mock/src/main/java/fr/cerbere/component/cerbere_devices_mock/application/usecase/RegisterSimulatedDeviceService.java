package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.RegisterSimulatedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;

import java.util.UUID;

/**
 * Implémentation du use-case d'enregistrement d'un device simulé.
 */
public final class RegisterSimulatedDeviceService implements RegisterSimulatedDeviceUseCase {

	private final SimulatedDeviceRepository simulatedDeviceRepository;

	public RegisterSimulatedDeviceService(final SimulatedDeviceRepository simulatedDeviceRepository) {
		this.simulatedDeviceRepository = simulatedDeviceRepository;
	}

	@Override
	public SimulatedDevice register(final DeviceType type, final String label) {
		final SimulatedDevice device = SimulatedDevice.register(UUID.randomUUID(), type, label);
		return this.simulatedDeviceRepository.save(device);
	}
}
