package fr.cerbere.component.cerbere_history.infrastructure.messaging.kafka.consumer;

import fr.cerbere.component.cerbere_history.domain.port.in.alarm.RecordAlertUseCase;
import fr.cerbere.component.cerbere_history.infrastructure.messaging.event.AlertRecordFactory;
import fr.cerbere.shared.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consomme {@code cerbere.alarm.alerts} pour historisation — voir
 * {@code DeviceEventHistoryKafkaConsumer} pour le principe de résilience.
 * {@code cerbere-core} ne conserve aucune copie locale des alertes (ADR 0006) :
 * cet enregistrement en est la source de vérité durable.
 */
@Component
@RequiredArgsConstructor
public final class AlertHistoryKafkaConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlertHistoryKafkaConsumer.class);

	private final RecordAlertUseCase recordAlertUseCase;

	@KafkaListener(
		topics = "cerbere.alarm.alerts",
		groupId = "${spring.application.name}",
		containerFactory = "eventEnvelopeKafkaListenerContainerFactory"
	)
	public void onMessage(final EventEnvelope envelope) {
		try {
			this.recordAlertUseCase.record(AlertRecordFactory.from(envelope));
		} catch (final RuntimeException exception) {
			LOGGER.error("Failed to historize alert (eventId={}): {}", envelope.eventId(), exception.getMessage());
		}
	}
}
