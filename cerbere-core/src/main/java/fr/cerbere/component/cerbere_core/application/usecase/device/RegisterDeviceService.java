package fr.cerbere.component.cerbere_core.application.usecase.device;

import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.DeviceType;
import fr.cerbere.component.cerbere_core.domain.port.in.device.RegisterDeviceUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Implémentation du use-case d'enregistrement d'un device.
 */
@RequiredArgsConstructor
public final class RegisterDeviceService implements RegisterDeviceUseCase {

	private final DeviceRepository deviceRepository;

	@Override
	public Device register(final UUID id, final DeviceType type, final String label, final UUID zoneId) {
		final Device device = Device.register(id, type, label, zoneId);
		return this.deviceRepository.save(device);
	}
}
