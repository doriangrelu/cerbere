package fr.cerbere.component.cerbere_core.adapter.config;

import fr.cerbere.component.cerbere_core.application.service.AlarmTriggerReevaluationService;
import fr.cerbere.component.cerbere_core.application.service.RecomputeZoneViolationService;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmStateChangedPublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmSystemRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.zone.ZoneRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Câblage des collaborateurs internes de {@code application.service} (couche
 * application, mais n'implémentant aucun port d'entrée : jamais appelés par un
 * adapter, uniquement par d'autres classes {@code application} — voir ADR 0018).
 * Distinct de {@link UseCaseConfig}, réservé aux ports d'entrée exposés.
 */
@Configuration(proxyBeanMethods = false)
public final class ApplicationServiceConfig {

	@Bean
	public RecomputeZoneViolationService recomputeZoneViolationService(final ZoneRepository zoneRepository, final DeviceRepository deviceRepository) {
		return new RecomputeZoneViolationService(zoneRepository, deviceRepository);
	}

	@Bean
	public AlarmTriggerReevaluationService alarmTriggerReevaluationService(final DeviceRepository deviceRepository,
																			final AlarmSystemRepository alarmSystemRepository,
																			final AlarmStateChangedPublisher alarmStateChangedPublisher) {
		return new AlarmTriggerReevaluationService(deviceRepository, alarmSystemRepository, alarmStateChangedPublisher);
	}
}
