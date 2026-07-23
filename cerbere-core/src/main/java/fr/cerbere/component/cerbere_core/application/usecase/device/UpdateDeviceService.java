package fr.cerbere.component.cerbere_core.application.usecase.device;

import fr.cerbere.component.cerbere_core.domain.event.DeviceUpdated;
import fr.cerbere.component.cerbere_core.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_core.domain.exception.DuplicateDeviceLabelException;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.port.in.device.UpdateDeviceUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DevicePublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Implémentation du use-case de modification d'un device. Publie {@link DeviceUpdated}
 * pour que {@code cerbere-devices-mock} garde son miroir aligné — voir ADR 0016.
 */
@RequiredArgsConstructor
public final class UpdateDeviceService implements UpdateDeviceUseCase {

	private final DeviceRepository deviceRepository;
	private final DevicePublisher publisher;

	@Override
	public Device update(final UUID id, final String label, final UUID zoneId, final boolean enabled) {
		final Device device = this.deviceRepository.findById(id)
			.orElseThrow(() -> new DeviceNotFoundException(id));
		this.deviceRepository.findByLabel(label)
			.filter(other -> !other.getId().equals(id))
			.ifPresent(other -> {
				throw new DuplicateDeviceLabelException(label);
			});
		final Device updated = device.withLabel(label).withZoneId(zoneId).withEnabled(enabled);
		final Device saved = this.deviceRepository.save(updated);
		this.publisher.publish(new DeviceUpdated(saved.getId(), saved.getLabel(), saved.getZoneId(), Instant.now(), UUID.randomUUID()));
		return saved;
	}
}
