package fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.kafka.producer;

import fr.cerbere.component.cerbere_devices_bridge.domain.event.DeviceEventOccurred;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DeviceEventPublisher;
import fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.event.DeviceEventEnvelopeFactory;
import fr.cerbere.shared.event.EventEnvelope;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Implémentation Kafka du port {@link DeviceEventPublisher}. Publie sur
 * {@code cerbere.device.events.raw} — topic déjà provisionné par
 * {@code cerbere-devices-mock} (ADR 0014), non redéclaré ici. Clé de
 * partition = {@code deviceId} (voir docs/best-practices/kafka-conventions.md).
 */
@Component
public final class DeviceEventKafkaProducer implements DeviceEventPublisher {

	public static final String TOPIC = "cerbere.device.events.raw";

	private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;

	public DeviceEventKafkaProducer(final KafkaTemplate<String, EventEnvelope> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public void publish(final DeviceEventOccurred event) {
		final EventEnvelope envelope = DeviceEventEnvelopeFactory.from(event);
		final String partitionKey = event.deviceId().toString();
		this.kafkaTemplate.send(TOPIC, partitionKey, envelope);
	}
}
