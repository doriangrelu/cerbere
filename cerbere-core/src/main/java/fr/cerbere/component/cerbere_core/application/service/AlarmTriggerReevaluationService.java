package fr.cerbere.component.cerbere_core.application.service;

import fr.cerbere.component.cerbere_core.domain.event.AlarmStateChanged;
import fr.cerbere.component.cerbere_core.domain.model.AlarmMode;
import fr.cerbere.component.cerbere_core.domain.model.AlarmSystem;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmStateChangedPublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmSystemRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Évalue si le système doit se déclencher au vu des devices actifs en
 * violation, sans jamais changer le mode d'armement courant. Partagé entre
 * {@code AlarmSystemService.arm()} (via {@link #anyEnabledDeviceViolating()})
 * et la réactivation d'un device déjà en violation pendant qu'il était inactif
 * (via {@link #reevaluate()}, appelé par {@code UpdateDeviceService}) : un
 * device désactivé reste supervisé (voir {@code HandleDeviceEventService})
 * mais ne peut jamais déclencher l'alarme tant qu'il est inactif — sa
 * réactivation doit donc réévaluer le déclenchement, sans quoi une violation
 * déjà connue passerait inaperçue jusqu'au prochain événement. Collaborateur
 * interne à la couche application : n'implémente aucun port d'entrée, aucun
 * adapter ne l'appelle directement — voir ADR 0018.
 */
@RequiredArgsConstructor
public final class AlarmTriggerReevaluationService {

	private final DeviceRepository deviceRepository;
	private final AlarmSystemRepository alarmSystemRepository;
	private final AlarmStateChangedPublisher alarmStateChangedPublisher;

	public boolean anyEnabledDeviceViolating() {
		return this.deviceRepository.findAll().stream()
			.filter(Device::isEnabled)
			.anyMatch(Device::isViolation);
	}

	public void reevaluate() {
		final AlarmSystem current = this.alarmSystemRepository.findById(AlarmSystem.DEFAULT_SYSTEM_ID)
			.orElseGet(() -> AlarmSystem.initial(AlarmSystem.DEFAULT_SYSTEM_ID));
		if (current.getMode() == AlarmMode.DISARMED || current.isTriggered()) {
			return;
		}
		if (this.anyEnabledDeviceViolating()) {
			final AlarmSystem saved = this.alarmSystemRepository.save(current.trigger());
			final AlarmStateChanged event = new AlarmStateChanged(
				saved.getId(), current.getMode(), saved.getMode(), saved.isTriggered(), Instant.now(), UUID.randomUUID());
			this.alarmStateChangedPublisher.publish(event);
		}
	}
}
