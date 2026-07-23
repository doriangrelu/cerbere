package fr.cerbere.component.cerbere_core.application.usecase.device;

import fr.cerbere.component.cerbere_core.domain.event.DeviceUpdated;
import fr.cerbere.component.cerbere_core.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_core.domain.exception.DuplicateDeviceLabelException;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.ReevaluateAlarmTriggerUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.device.UpdateDeviceUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.zone.RecomputeZoneViolationUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DevicePublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Implémentation du use-case de modification d'un device. Publie {@link DeviceUpdated}
 * pour que {@code cerbere-devices-mock} garde son miroir aligné — voir ADR 0016.
 * Si {@code zoneId} change, recalcule l'état {@code violation} de l'ancienne
 * et de la nouvelle zone (un device en violation peut en quitter ou en rejoindre
 * une — voir ADR 0017). Un device peut rester en violation pendant qu'il est
 * désactivé (supervision continue, voir {@code HandleDeviceEventService}) sans
 * jamais déclencher l'alarme tant qu'il est inactif ; sa réactivation doit donc
 * réévaluer le déclenchement — sans quoi une violation déjà connue passerait
 * inaperçue jusqu'au prochain événement.
 */
@RequiredArgsConstructor
public final class UpdateDeviceService implements UpdateDeviceUseCase {

	private final DeviceRepository deviceRepository;
	private final DevicePublisher publisher;
	private final RecomputeZoneViolationUseCase recomputeZoneViolationUseCase;
	private final ReevaluateAlarmTriggerUseCase reevaluateAlarmTriggerUseCase;

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
		final Device updated = device.withLabel(label).withZoneId(zoneId).withEnabled(enabled);
		final Device saved = this.deviceRepository.save(updated);
		this.publisher.publish(new DeviceUpdated(saved.getId(), saved.getLabel(), saved.getZoneId(), Instant.now(), UUID.randomUUID()));
		this.recomputeZoneViolationUseCase.recompute(previousZoneId);
		if (!Objects.equals(previousZoneId, saved.getZoneId())) {
			this.recomputeZoneViolationUseCase.recompute(saved.getZoneId());
		}
		if (!wasEnabled && enabled) {
			this.reevaluateAlarmTriggerUseCase.reevaluate();
		}
		return saved;
	}
}
