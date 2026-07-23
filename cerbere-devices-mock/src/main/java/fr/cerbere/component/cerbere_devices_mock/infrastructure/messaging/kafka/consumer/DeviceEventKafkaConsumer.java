package fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.kafka.consumer;

import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.DeleteSimulatedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.RegisterSimulatedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.UpdateSimulatedDeviceUseCase;
import fr.cerbere.shared.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consomme {@code cerbere.device.state}, publié par {@code cerbere-core}, pour
 * garder le miroir local aligné sur le registre officiel (voir ADR 0016).
 * Résilient : un message inattendu (type inconnu, payload incomplet) est loggé
 * et ignoré plutôt que de faire échouer/bloquer le listener.
 */
@Component
@RequiredArgsConstructor
public final class DeviceEventKafkaConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceEventKafkaConsumer.class);

	private final RegisterSimulatedDeviceUseCase registerSimulatedDevice;
	private final UpdateSimulatedDeviceUseCase updateSimulatedDevice;
	private final DeleteSimulatedDeviceUseCase deleteSimulatedDevice;

	@KafkaListener(
			topics = "cerbere.device.state",
			groupId = "${spring.application.name}",
			containerFactory = "eventEnvelopeKafkaListenerContainerFactory"
	)
	public void onMessage(final EventEnvelope envelope) {
		try {
			switch (envelope.eventType()) {
				case "device.created" -> this.registerSimulatedDevice.register(
						envelope.deviceId(),
						DeviceType.valueOf(envelope.getPayloadValue("type").orElseThrow()),
						envelope.getPayloadValue("label").orElseThrow(),
						envelope.zoneId(),
						false
				);
				case "device.updated" -> this.updateSimulatedDevice.update(
						envelope.deviceId(),
						envelope.getPayloadValue("label").orElseThrow(),
						envelope.zoneId()
				);
				case "device.deleted" -> this.deleteSimulatedDevice.delete(envelope.deviceId());
				default -> LOGGER.warn("Unsupported event type: {}", envelope.eventType());
			}
		} catch (final RuntimeException exception) {
			LOGGER.error("Failed to process device event {} (deviceId={}): {}",
					envelope.eventType(), envelope.deviceId(), exception.getMessage());
		}
	}
}
