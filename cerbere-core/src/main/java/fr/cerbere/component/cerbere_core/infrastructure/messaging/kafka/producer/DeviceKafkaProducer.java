package fr.cerbere.component.cerbere_core.infrastructure.messaging.kafka.producer;

import fr.cerbere.component.cerbere_core.domain.event.DeviceCreated;
import fr.cerbere.component.cerbere_core.domain.event.DeviceDeleted;
import fr.cerbere.component.cerbere_core.domain.event.DeviceUpdated;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DevicePublisher;
import fr.cerbere.component.cerbere_core.infrastructure.messaging.event.DeviceEventFactory;
import fr.cerbere.shared.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceKafkaProducer implements DevicePublisher {

    public static final String TOPIC = "cerbere.device.state";

    private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;

    @Override
    public void publish(final DeviceCreated event) {
        final EventEnvelope eventEnvelope = DeviceEventFactory.from(event);
        final String partitionKey = event.zoneId() != null ? event.zoneId().toString() : event.id().toString();
        this.kafkaTemplate.send(TOPIC, partitionKey, eventEnvelope);
    }

    @Override
    public void publish(final DeviceUpdated event) {
        final EventEnvelope eventEnvelope = DeviceEventFactory.from(event);
        final String partitionKey = event.zoneId() != null ? event.zoneId().toString() : event.id().toString();
        this.kafkaTemplate.send(TOPIC, partitionKey, eventEnvelope);
    }

    @Override
    public void publish(final DeviceDeleted event) {
        final EventEnvelope eventEnvelope = DeviceEventFactory.from(event);
        this.kafkaTemplate.send(TOPIC, event.id().toString(), eventEnvelope);
    }

}
