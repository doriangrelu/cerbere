package fr.cerbere.component.cerbere_core.infrastructure.messaging.kafka.producer;

import fr.cerbere.component.cerbere_core.domain.event.AlertRaised;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlertPublisher;
import fr.cerbere.component.cerbere_core.infrastructure.messaging.event.AlertEnvelopeFactory;
import fr.cerbere.shared.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Implémentation Kafka du port {@link AlertPublisher}. Publie sur
 * {@code cerbere.alarm.alerts}, clé de partition = {@code zoneId} si présent,
 * sinon {@code deviceId} (voir docs/best-practices/kafka-conventions.md).
 */
@Component
@RequiredArgsConstructor
public final class AlertKafkaProducer implements AlertPublisher {

	public static final String TOPIC = "cerbere.alarm.alerts";

	private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;

	@Override
	public void publish(final AlertRaised event) {
		final EventEnvelope envelope = AlertEnvelopeFactory.from(event);
		final String partitionKey = event.zoneId() != null ? event.zoneId().toString() : event.deviceId().toString();
		this.kafkaTemplate.send(TOPIC, partitionKey, envelope);
	}
}
