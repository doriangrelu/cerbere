package fr.cerbere.component.cerbere_core.application.usecase.device;

import fr.cerbere.component.cerbere_core.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.port.in.device.UpdateDeviceUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Implémentation du use-case de modification d'un device.
 */
@RequiredArgsConstructor
public final class UpdateDeviceService implements UpdateDeviceUseCase {

	private final DeviceRepository deviceRepository;

	@Override
	public Device update(final UUID id, final String label, final UUID zoneId, final boolean enabled) {
		final Device device = this.deviceRepository.findById(id)
			.orElseThrow(() -> new DeviceNotFoundException(id));
		final Device updated = device.withLabel(label).withZoneId(zoneId).withEnabled(enabled);
		return this.deviceRepository.save(updated);
	}
}
