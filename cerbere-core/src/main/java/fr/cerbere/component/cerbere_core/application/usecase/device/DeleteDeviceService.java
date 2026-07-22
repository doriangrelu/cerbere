package fr.cerbere.component.cerbere_core.application.usecase.device;

import fr.cerbere.component.cerbere_core.domain.port.in.device.DeleteDeviceUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Implémentation du use-case de suppression d'un device.
 */
@RequiredArgsConstructor
public final class DeleteDeviceService implements DeleteDeviceUseCase {

	private final DeviceRepository deviceRepository;

	@Override
	public void delete(final UUID id) {
		this.deviceRepository.deleteById(id);
	}
}
