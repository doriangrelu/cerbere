package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.RenameSimulatedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;

/**
 * Implémentation du renommage (appairage) — voir ADR 0021/0023. Le device est
 * retrouvé par son {@code friendlyName} courant, comme le ferait la vraie
 * passerelle Zigbee2MQTT en traitant {@code bridge/request/device/rename}.
 */
public final class RenameSimulatedDeviceService implements RenameSimulatedDeviceUseCase {

	private final SimulatedDeviceRepository simulatedDeviceRepository;

	public RenameSimulatedDeviceService(final SimulatedDeviceRepository simulatedDeviceRepository) {
		this.simulatedDeviceRepository = simulatedDeviceRepository;
	}

	@Override
	public SimulatedDevice rename(final String currentFriendlyName, final String newFriendlyName) {
		final SimulatedDevice device = this.simulatedDeviceRepository.findByFriendlyName(currentFriendlyName)
			.orElseThrow(() -> new DeviceNotFoundException(currentFriendlyName));
		return this.simulatedDeviceRepository.save(device.withFriendlyName(newFriendlyName));
	}
}
