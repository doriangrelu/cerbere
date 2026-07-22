package fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.kafka.producer;

import fr.cerbere.component.cerbere_devices_mock.domain.event.DeviceEventOccurred;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.DeviceEventPublisher;
import fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.event.DeviceEventEnvelopeFactory;
import fr.cerbere.shared.event.EventEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Implémentation Kafka du port {@link DeviceEventPublisher}. Publie sur
 * {@code cerbere.device.events.raw}, clé de partition = {@code deviceId}
 * (voir docs/best-practices/kafka-conventions.md).
 */
@Slf4j
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
