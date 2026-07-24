package fr.cerbere.component.cerbere_history.infrastructure.messaging.kafka.consumer;

import fr.cerbere.component.cerbere_history.domain.port.in.device.RecordDeviceEventUseCase;
import fr.cerbere.component.cerbere_history.infrastructure.messaging.event.DeviceEventRecordFactory;
import fr.cerbere.shared.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consomme {@code cerbere.device.events.raw} pour historisation. Encapsule le
 * traitement dans un try/catch résilient : {@code cerbere-history} est la
 * seule couche de durabilité des événements Kafka (rétention 3 jours, voir
 * ADR 0014), un listener qui s'arrête créerait un vrai trou d'historique.
 */
@Component
@RequiredArgsConstructor
public final class DeviceEventHistoryKafkaConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceEventHistoryKafkaConsumer.class);

	private final RecordDeviceEventUseCase recordDeviceEventUseCase;

	@KafkaListener(
		topics = "cerbere.device.events.raw",
		groupId = "${spring.application.name}",
		containerFactory = "eventEnvelopeKafkaListenerContainerFactory"
	)
	public void onMessage(final EventEnvelope envelope) {
		try {
			this.recordDeviceEventUseCase.record(DeviceEventRecordFactory.from(envelope));
		} catch (final RuntimeException exception) {
			LOGGER.error("Failed to historize device event {} (deviceId={}): {}",
				envelope.eventType(), envelope.deviceId(), exception.getMessage());
		}
	}
}
