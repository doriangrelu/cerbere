package fr.cerbere.component.cerbere_core.application.usecase.device;

import fr.cerbere.component.cerbere_core.domain.event.DeviceSupervisionChanged;
import fr.cerbere.component.cerbere_core.domain.event.DeviceUpdated;
import fr.cerbere.component.cerbere_core.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_core.domain.exception.DuplicateDeviceLabelException;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.port.in.device.UpdateDeviceUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DevicePublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceSupervisionChangedPublisher;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Implémentation du use-case de modification d'un device. Publie {@link DeviceUpdated}
 * pour que {@code cerbere-devices-bridge} garde son miroir aligné — voir ADR 0016.
 * <p>
 * Une modification peut changer la donne pour l'alarme de deux façons : le device
 * change de zone (l'ancienne comme la nouvelle doivent être recalculées, voir
 * ADR 0017), ou le device est réactivé alors qu'il était resté en violation
 * pendant qu'il était inactif — un device désactivé reste supervisé (voir
 * {@code HandleDeviceEventService}) sans jamais déclencher l'alarme tant qu'il
 * est inactif, sa réactivation doit donc réévaluer le déclenchement. Plutôt que
 * de distinguer ces cas ici, le use-case émet systématiquement
 * {@link DeviceSupervisionChanged} avec l'ancienne et la nouvelle zone :
 * {@code ReevaluateAlarmStateService} est seul juge des conséquences, et sa
 * réévaluation est idempotente (voir ADR 0025).
 */
@RequiredArgsConstructor
public final class UpdateDeviceService implements UpdateDeviceUseCase {

	private final DeviceRepository deviceRepository;
	private final DevicePublisher publisher;
	private final DeviceSupervisionChangedPublisher supervisionChangedPublisher;

	@Override
	public Device update(final UUID id, final String label, final UUID zoneId, final boolean enabled) {
		final Device device = this.deviceRepository.findById(id)
			.orElseThrow(() -> new DeviceNotFoundException(id));
		this.deviceRepository.findByLabel(label)
			.filter(other -> !other.getId().equals(id))
			.ifPresent(other -> {
				throw new DuplicateDeviceLabelException(label);
			});
		final UUID previousZoneId = device.getZoneId();
		final boolean wasEnabled = device.isEnabled();
		Device updated = device.withLabel(label).withZoneId(zoneId).withEnabled(enabled);
		if (!wasEnabled && enabled) {
			updated = updated.withLastSeenAt(Instant.now());
		}
		final Device saved = this.deviceRepository.save(updated);
		this.publisher.publish(new DeviceUpdated(saved.getId(), saved.getLabel(), saved.getZoneId(), Instant.now(), UUID.randomUUID()));
		this.supervisionChangedPublisher.publish(
			DeviceSupervisionChanged.forDevice(saved.getId(), previousZoneId, saved.getZoneId()));
		return saved;
	}
}
