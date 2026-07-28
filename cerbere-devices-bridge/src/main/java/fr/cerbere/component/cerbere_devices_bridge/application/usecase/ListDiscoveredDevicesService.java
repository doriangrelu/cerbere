package fr.cerbere.component.cerbere_devices_bridge.application.usecase;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.DiscoveredDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.ListDiscoveredDevicesUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DiscoveredDeviceRepository;

import java.util.List;

/**
 * Implémentation du use-case de consultation des devices en attente d'appairage.
 */
public final class ListDiscoveredDevicesService implements ListDiscoveredDevicesUseCase {

	private final DiscoveredDeviceRepository discoveredDeviceRepository;

	public ListDiscoveredDevicesService(final DiscoveredDeviceRepository discoveredDeviceRepository) {
		this.discoveredDeviceRepository = discoveredDeviceRepository;
	}

	@Override
	public List<DiscoveredDevice> listAll() {
		return this.discoveredDeviceRepository.findAll();
	}
}
