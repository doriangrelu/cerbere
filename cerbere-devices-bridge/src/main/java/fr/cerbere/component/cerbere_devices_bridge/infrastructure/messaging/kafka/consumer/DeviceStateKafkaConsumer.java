package fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.kafka.consumer;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.DeleteBridgedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.RegisterBridgedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.UpdateBridgedDeviceUseCase;
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
public final class DeviceStateKafkaConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceStateKafkaConsumer.class);

	private final RegisterBridgedDeviceUseCase registerBridgedDevice;
	private final UpdateBridgedDeviceUseCase updateBridgedDevice;
	private final DeleteBridgedDeviceUseCase deleteBridgedDevice;

	@KafkaListener(
		topics = "cerbere.device.state",
		groupId = "${spring.application.name}",
		containerFactory = "eventEnvelopeKafkaListenerContainerFactory"
	)
	public void onMessage(final EventEnvelope envelope) {
		try {
			switch (envelope.eventType()) {
				case "device.created" -> this.registerBridgedDevice.register(
					envelope.deviceId(),
					DeviceType.valueOf(envelope.getPayloadValue("type").orElseThrow()),
					envelope.getPayloadValue("label").orElseThrow(),
					envelope.zoneId()
				);
				case "device.updated" -> this.updateBridgedDevice.update(
					envelope.deviceId(),
					envelope.getPayloadValue("label").orElseThrow(),
					envelope.zoneId()
				);
				case "device.deleted" -> this.deleteBridgedDevice.delete(envelope.deviceId());
				default -> LOGGER.warn("Unsupported event type: {}", envelope.eventType());
			}
		} catch (final RuntimeException exception) {
			LOGGER.error("Failed to process device state event {} (deviceId={}): {}",
				envelope.eventType(), envelope.deviceId(), exception.getMessage());
		}
	}
}
