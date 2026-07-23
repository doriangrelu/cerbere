package fr.cerbere.component.cerbere_core.application.usecase.device;

import fr.cerbere.component.cerbere_core.domain.event.DeviceDeleted;
import fr.cerbere.component.cerbere_core.domain.port.in.device.DeleteDeviceUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DevicePublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Implémentation du use-case de suppression d'un device. Publie {@link DeviceDeleted}
 * pour que {@code cerbere-devices-mock} retire son miroir correspondant — voir ADR 0016.
 */
@RequiredArgsConstructor
public final class DeleteDeviceService implements DeleteDeviceUseCase {

	private final DeviceRepository deviceRepository;
	private final DevicePublisher publisher;

	@Override
	public void delete(final UUID id) {
		this.deviceRepository.deleteById(id);
		this.publisher.publish(new DeviceDeleted(id, Instant.now(), UUID.randomUUID()));
	}
}
