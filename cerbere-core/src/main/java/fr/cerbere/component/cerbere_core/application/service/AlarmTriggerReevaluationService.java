package fr.cerbere.component.cerbere_core.application.service;

import fr.cerbere.component.cerbere_core.domain.event.AlarmStateChanged;
import fr.cerbere.component.cerbere_core.domain.exception.ConcurrentAlarmSystemUpdateException;
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
 * et {@link #reevaluate()}, appelé pour chaque device dont la supervision
 * change (voir ADR 0025) — un device désactivé reste supervisé (voir
 * {@code HandleDeviceEventService}) mais ne peut jamais déclencher l'alarme
 * tant qu'il est inactif, sa réactivation doit donc réévaluer le
 * déclenchement, sans quoi une violation déjà connue passerait inaperçue
 * jusqu'au prochain événement. Collaborateur interne à la couche application :
 * n'implémente aucun port d'entrée, aucun adapter ne l'appelle directement —
 * voir ADR 0018.
 * <p>
 * {@code AlarmSystem} est un document unique ({@code home-1}) : le scheduler de
 * supervision de vie (toutes les {@code check-interval-ms}) et le traitement de
 * chaque événement de device y écrivent tous deux concurremment dès qu'un
 * device quelque part est en violation. Une collision de verrouillage
 * optimiste ({@link ConcurrentAlarmSystemUpdateException}) y est donc attendue,
 * pas exceptionnelle : {@link #reevaluate()} relit l'état courant et retente sa
 * décision plutôt que de laisser filer silencieusement un déclenchement
 * manqué.
 */
@RequiredArgsConstructor
public final class AlarmTriggerReevaluationService {

	private static final int MAX_ATTEMPTS = 5;

	private final DeviceRepository deviceRepository;
	private final AlarmSystemRepository alarmSystemRepository;
	private final AlarmStateChangedPublisher alarmStateChangedPublisher;

	public boolean anyEnabledDeviceViolating() {
		return this.deviceRepository.findAll().stream()
			.filter(Device::isEnabled)
			.anyMatch(Device::isViolation);
	}

	public void reevaluate() {
		for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
			try {
				this.tryReevaluate();
				return;
			} catch (final ConcurrentAlarmSystemUpdateException exception) {
				if (attempt == MAX_ATTEMPTS) {
					throw exception;
				}
			}
		}
	}

	private void tryReevaluate() {
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
