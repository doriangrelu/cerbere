package fr.cerbere.component.cerbere_core.application.usecase.device;

import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.port.in.device.ListDevicesUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Implémentation du use-case de consultation des devices.
 */
@RequiredArgsConstructor
public final class ListDevicesService implements ListDevicesUseCase {

	private final DeviceRepository deviceRepository;

	@Override
	public List<Device> listAll() {
		return this.deviceRepository.findAll();
	}
}
