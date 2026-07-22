package fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.kafka.consumer;

import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.RegisterSimulatedDeviceUseCase;
import fr.cerbere.shared.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * TODO améliorer le traitement de l'évènement. Améliorer également la résilience dans le cas ou le message est incomplet
 */
@Component
@RequiredArgsConstructor
public final class DeviceEventKafkaConsumer {

    private final RegisterSimulatedDeviceUseCase registerSimulatedDevice;

    @KafkaListener(
            topics = "cerbere.device.state",
            groupId = "${spring.application.name}",
            containerFactory = "eventEnvelopeKafkaListenerContainerFactory"
    )
    public void onMessage(final EventEnvelope envelope) {
        if (envelope.eventType().equals("device.created")) {
            this.registerSimulatedDevice.register(
                    envelope.deviceId(),
                    DeviceType.valueOf(envelope.getPayloadValue("type").orElseThrow()),
                    envelope.getPayloadValue("label").orElseThrow(),
                    envelope.zoneId(),
                    false
            );
        } else {
            throw new UnsupportedOperationException("Unsupported event type: " + envelope.eventType());
        }
    }
}
