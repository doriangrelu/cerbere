package fr.cerbere.component.cerbere_history.infrastructure.messaging.kafka.consumer;

import fr.cerbere.component.cerbere_history.domain.port.in.alarm.RecordAlarmStateChangeUseCase;
import fr.cerbere.component.cerbere_history.infrastructure.messaging.event.AlarmStateChangeRecordFactory;
import fr.cerbere.shared.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consomme {@code cerbere.alarm.state-changed} pour historisation — voir
 * {@code DeviceEventHistoryKafkaConsumer} pour le principe de résilience.
 */
@Component
@RequiredArgsConstructor
public final class AlarmStateChangeHistoryKafkaConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlarmStateChangeHistoryKafkaConsumer.class);

	private final RecordAlarmStateChangeUseCase recordAlarmStateChangeUseCase;

	@KafkaListener(
		topics = "cerbere.alarm.state-changed",
		groupId = "${spring.application.name}",
		containerFactory = "eventEnvelopeKafkaListenerContainerFactory"
	)
	public void onMessage(final EventEnvelope envelope) {
		try {
			this.recordAlarmStateChangeUseCase.record(AlarmStateChangeRecordFactory.from(envelope));
		} catch (final RuntimeException exception) {
			LOGGER.error("Failed to historize alarm state change (eventId={}): {}", envelope.eventId(), exception.getMessage());
		}
	}
}
