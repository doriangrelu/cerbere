package fr.cerbere.component.cerbere_core.infrastructure.messaging.kafka.producer;

import fr.cerbere.component.cerbere_core.domain.event.AlarmStateChanged;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmStateChangedPublisher;
import fr.cerbere.component.cerbere_core.infrastructure.messaging.event.AlarmStateChangedEnvelopeFactory;
import fr.cerbere.shared.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Implémentation Kafka du port {@link AlarmStateChangedPublisher}. Publie sur
 * {@code cerbere.alarm.state-changed}, clé de partition = {@code systemId}
 * (voir docs/best-practices/kafka-conventions.md).
 */
@Component
@RequiredArgsConstructor
public final class AlarmStateChangedKafkaProducer implements AlarmStateChangedPublisher {

	public static final String TOPIC = "cerbere.alarm.state-changed";

	private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;

	@Override
	public void publish(final AlarmStateChanged event) {
		final EventEnvelope envelope = AlarmStateChangedEnvelopeFactory.from(event);
		this.kafkaTemplate.send(TOPIC, event.systemId(), envelope);
	}
}
