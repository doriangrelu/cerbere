package fr.cerbere.component.cerbere_history.adapter.config;

import fr.cerbere.component.cerbere_history.application.usecase.alarm.AlarmStateChangeHistoryService;
import fr.cerbere.component.cerbere_history.application.usecase.alarm.AlertHistoryService;
import fr.cerbere.component.cerbere_history.application.usecase.device.DeviceEventHistoryService;
import fr.cerbere.component.cerbere_history.domain.port.in.alarm.ListAlarmStateChangesUseCase;
import fr.cerbere.component.cerbere_history.domain.port.in.alarm.ListAlertsUseCase;
import fr.cerbere.component.cerbere_history.domain.port.in.alarm.RecordAlarmStateChangeUseCase;
import fr.cerbere.component.cerbere_history.domain.port.in.alarm.RecordAlertUseCase;
import fr.cerbere.component.cerbere_history.domain.port.in.device.ListDeviceEventsUseCase;
import fr.cerbere.component.cerbere_history.domain.port.in.device.RecordDeviceEventUseCase;
import fr.cerbere.component.cerbere_history.domain.port.out.alarm.AlarmStateChangeRecordRepository;
import fr.cerbere.component.cerbere_history.domain.port.out.alarm.AlertRecordRepository;
import fr.cerbere.component.cerbere_history.domain.port.out.device.DeviceEventRecordRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Câblage des use-cases (couche application) contre leurs ports d'entrée. Les
 * classes de {@code application.usecase} restent volontairement dépourvues
 * d'annotations Spring (voir ADR 0001) : leur instanciation et leur exposition
 * en tant que bean se font ici.
 */
@Configuration(proxyBeanMethods = false)
public final class UseCaseConfig {

	@Bean
	public DeviceEventHistoryService deviceEventHistoryService(final DeviceEventRecordRepository deviceEventRecordRepository) {
		return new DeviceEventHistoryService(deviceEventRecordRepository);
	}

	@Bean
	public RecordDeviceEventUseCase recordDeviceEventUseCase(final DeviceEventHistoryService deviceEventHistoryService) {
		return deviceEventHistoryService;
	}

	@Bean
	public ListDeviceEventsUseCase listDeviceEventsUseCase(final DeviceEventHistoryService deviceEventHistoryService) {
		return deviceEventHistoryService;
	}

	@Bean
	public AlarmStateChangeHistoryService alarmStateChangeHistoryService(final AlarmStateChangeRecordRepository alarmStateChangeRecordRepository) {
		return new AlarmStateChangeHistoryService(alarmStateChangeRecordRepository);
	}

	@Bean
	public RecordAlarmStateChangeUseCase recordAlarmStateChangeUseCase(final AlarmStateChangeHistoryService alarmStateChangeHistoryService) {
		return alarmStateChangeHistoryService;
	}

	@Bean
	public ListAlarmStateChangesUseCase listAlarmStateChangesUseCase(final AlarmStateChangeHistoryService alarmStateChangeHistoryService) {
		return alarmStateChangeHistoryService;
	}

	@Bean
	public AlertHistoryService alertHistoryService(final AlertRecordRepository alertRecordRepository) {
		return new AlertHistoryService(alertRecordRepository);
	}

	@Bean
	public RecordAlertUseCase recordAlertUseCase(final AlertHistoryService alertHistoryService) {
		return alertHistoryService;
	}

	@Bean
	public ListAlertsUseCase listAlertsUseCase(final AlertHistoryService alertHistoryService) {
		return alertHistoryService;
	}
}
