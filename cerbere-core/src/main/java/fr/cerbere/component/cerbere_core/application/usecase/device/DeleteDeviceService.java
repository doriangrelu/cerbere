package fr.cerbere.component.cerbere_core.application.usecase.device;

import fr.cerbere.component.cerbere_core.domain.event.DeviceDeleted;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.port.in.device.DeleteDeviceUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.zone.RecomputeZoneViolationUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DevicePublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Implémentation du use-case de suppression d'un device. Publie {@link DeviceDeleted}
 * pour que {@code cerbere-devices-mock} retire son miroir correspondant — voir ADR 0016.
 * Recalcule l'état {@code violation} de la zone de rattachement après suppression
 * (un device en violation peut en être le seul contributeur — voir ADR 0017).
 */
@RequiredArgsConstructor
public final class DeleteDeviceService implements DeleteDeviceUseCase {

	private final DeviceRepository deviceRepository;
	private final DevicePublisher publisher;
	private final RecomputeZoneViolationUseCase recomputeZoneViolationUseCase;

	@Override
	public void delete(final UUID id) {
		final UUID zoneId = this.deviceRepository.findById(id).map(Device::getZoneId).orElse(null);
		this.deviceRepository.deleteById(id);
		this.publisher.publish(new DeviceDeleted(id, Instant.now(), UUID.randomUUID()));
		this.recomputeZoneViolationUseCase.recompute(zoneId);
	}
}
