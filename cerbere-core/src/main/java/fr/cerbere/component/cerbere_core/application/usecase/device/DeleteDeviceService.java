package fr.cerbere.component.cerbere_core.application.usecase.device;

import fr.cerbere.component.cerbere_core.domain.event.DeviceDeleted;
import fr.cerbere.component.cerbere_core.domain.event.DeviceSupervisionChanged;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.port.in.device.DeleteDeviceUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DevicePublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceSupervisionChangedPublisher;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Implémentation du use-case de suppression d'un device. Publie {@link DeviceDeleted}
 * pour que {@code cerbere-devices-bridge} retire son miroir correspondant — voir ADR 0016.
 * Émet en plus {@link DeviceSupervisionChanged} sur la zone de rattachement : le
 * device supprimé pouvait en être le seul contributeur en violation (voir
 * ADR 0017), c'est à {@code ReevaluateAlarmStateService} de la recalculer
 * (voir ADR 0025).
 */
@RequiredArgsConstructor
public final class DeleteDeviceService implements DeleteDeviceUseCase {

	private final DeviceRepository deviceRepository;
	private final DevicePublisher publisher;
	private final DeviceSupervisionChangedPublisher supervisionChangedPublisher;

	@Override
	public void delete(final UUID id) {
		final UUID zoneId = this.deviceRepository.findById(id).map(Device::getZoneId).orElse(null);
		this.deviceRepository.deleteById(id);
		this.publisher.publish(new DeviceDeleted(id, Instant.now(), UUID.randomUUID()));
		this.supervisionChangedPublisher.publish(DeviceSupervisionChanged.forDevice(id, zoneId));
	}
}
