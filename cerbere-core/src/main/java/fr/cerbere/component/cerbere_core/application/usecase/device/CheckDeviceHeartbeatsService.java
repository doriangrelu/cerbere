package fr.cerbere.component.cerbere_core.application.usecase.device;

import fr.cerbere.component.cerbere_core.application.service.AlarmTriggerReevaluationService;
import fr.cerbere.component.cerbere_core.application.service.RecomputeZoneViolationService;
import fr.cerbere.component.cerbere_core.domain.event.AlertRaised;
import fr.cerbere.component.cerbere_core.domain.event.AlertSeverity;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.port.in.device.CheckDeviceHeartbeatsUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlertPublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Implémentation de la supervision de vie (voir ADR 0020, restreinte aux devices
 * appairés par ADR 0022). Un device actif, déjà appairé ({@code linked}) et pas
 * déjà en violation dont {@code lastSeenAt} dépasse le délai configuré est
 * marqué en violation (même état que pour une vraie violation capteur, la
 * zone/l'alarme sont recalculées de la même façon), et une alerte dédiée est
 * levée. Un device jamais appairé est ignoré : il n'a jamais eu l'occasion de
 * communiquer, ce n'est pas une violation, seulement une provision en attente
 * (voir ADR 0022). Un device déjà en violation n'est pas retraité à chaque
 * passage du scheduler (évite de relever une alerte en boucle) — il redevient
 * supervisé normalement dès qu'un nouvel événement le fait ressortir de la
 * violation (voir {@code HandleDeviceEventService}).
 */
public final class CheckDeviceHeartbeatsService implements CheckDeviceHeartbeatsUseCase {

	private final DeviceRepository deviceRepository;
	private final RecomputeZoneViolationService recomputeZoneViolationService;
	private final AlarmTriggerReevaluationService alarmTriggerReevaluationService;
	private final AlertPublisher alertPublisher;
	private final Duration timeout;

	public CheckDeviceHeartbeatsService(final DeviceRepository deviceRepository,
										 final RecomputeZoneViolationService recomputeZoneViolationService,
										 final AlarmTriggerReevaluationService alarmTriggerReevaluationService,
										 final AlertPublisher alertPublisher,
										 final Duration timeout) {
		this.deviceRepository = deviceRepository;
		this.recomputeZoneViolationService = recomputeZoneViolationService;
		this.alarmTriggerReevaluationService = alarmTriggerReevaluationService;
		this.alertPublisher = alertPublisher;
		this.timeout = timeout;
	}

	@Override
	public void check() {
		final Instant now = Instant.now();
		this.deviceRepository.findAll().stream()
			.filter(Device::isEnabled)
			.filter(Device::isLinked)
			.filter(device -> !device.isViolation())
			.filter(device -> this.isStale(device, now))
			.forEach(device -> this.markUnreachable(device, now));
	}

	private boolean isStale(final Device device, final Instant now) {
		return Duration.between(device.getLastSeenAt(), now).compareTo(this.timeout) > 0;
	}

	private void markUnreachable(final Device device, final Instant now) {
		final Device saved = this.deviceRepository.save(device.withViolation());
		this.recomputeZoneViolationService.recompute(saved.getZoneId());
		this.alarmTriggerReevaluationService.reevaluate();
		final AlertRaised alert = new AlertRaised(
			UUID.randomUUID(),
			saved.getZoneId(),
			saved.getId(),
			AlertSeverity.WARNING,
			"Device injoignable depuis plus de " + this.timeout.toMinutes() + " min : " + saved.getLabel(),
			now,
			UUID.randomUUID()
		);
		this.alertPublisher.publish(alert);
	}
}
