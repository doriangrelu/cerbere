package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.RenameSimulatedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;

import java.util.UUID;

/**
 * Implémentation du use-case de renommage (appairage) — voir ADR 0021.
 */
public final class RenameSimulatedDeviceService implements RenameSimulatedDeviceUseCase {

	private final SimulatedDeviceRepository simulatedDeviceRepository;

	public RenameSimulatedDeviceService(final SimulatedDeviceRepository simulatedDeviceRepository) {
		this.simulatedDeviceRepository = simulatedDeviceRepository;
	}

	@Override
	public SimulatedDevice rename(final UUID id, final String newFriendlyName) {
		final SimulatedDevice device = this.simulatedDeviceRepository.findById(id)
			.orElseThrow(() -> new DeviceNotFoundException(id));
		return this.simulatedDeviceRepository.save(device.withFriendlyName(newFriendlyName));
	}
}
