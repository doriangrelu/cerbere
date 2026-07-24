package fr.cerbere.component.cerbere_devices_bridge.application.usecase;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.BridgedDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.ListBridgedDevicesUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.BridgedDeviceRepository;

import java.util.List;

/**
 * Implémentation du use-case de consultation des devices physiques connus.
 */
public final class ListBridgedDevicesService implements ListBridgedDevicesUseCase {

	private final BridgedDeviceRepository bridgedDeviceRepository;

	public ListBridgedDevicesService(final BridgedDeviceRepository bridgedDeviceRepository) {
		this.bridgedDeviceRepository = bridgedDeviceRepository;
	}

	@Override
	public List<BridgedDevice> listAll() {
		return this.bridgedDeviceRepository.findAll();
	}
}
