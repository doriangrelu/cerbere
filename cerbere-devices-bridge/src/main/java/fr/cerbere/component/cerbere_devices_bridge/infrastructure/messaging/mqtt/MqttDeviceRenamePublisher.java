package fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.mqtt;

import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DeviceRenamePublisher;
import fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.mqtt.payload.DeviceRenameRequest;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Implémentation MQTT du port {@link DeviceRenamePublisher} : publie sur
 * {@code <base-topic>/bridge/request/device/rename}, l'API bridge standard de
 * Zigbee2MQTT (voir docs/architecture/mqtt-zigbee-contracts.md). Le destinataire
 * est la vraie passerelle Zigbee2MQTT en profil {@code hardware}, ou
 * {@code cerbere-devices-mock} qui en joue le rôle en profil {@code mock}
 * (ADR 0021) — le Bridge ne fait aucune différence entre les deux.
 */
@Component
public class MqttDeviceRenamePublisher implements DeviceRenamePublisher {

	private static final String RENAME_TOPIC_SUFFIX = "/bridge/request/device/rename";
	private static final int QOS = 1;

	private final MqttClient mqttClient;
	private final ObjectMapper objectMapper;
	private final String baseTopic;

	public MqttDeviceRenamePublisher(final MqttClient mqttClient,
									  @Qualifier("objectMapper") final ObjectMapper objectMapper,
									  @Value("${cerbere.devices-bridge.mqtt.base-topic}") final String baseTopic) {
		this.mqttClient = mqttClient;
		this.objectMapper = objectMapper;
		this.baseTopic = baseTopic;
	}

	@Override
	public void rename(final String currentFriendlyName, final String newFriendlyName) {
		try {
			final byte[] payload = this.objectMapper.writeValueAsBytes(new DeviceRenameRequest(currentFriendlyName, newFriendlyName));
			final MqttMessage message = new MqttMessage(payload);
			message.setQos(QOS);
			this.mqttClient.publish(this.baseTopic + RENAME_TOPIC_SUFFIX, message);
		} catch (final MqttException exception) {
			throw new IllegalStateException("Failed to publish MQTT rename request for device " + currentFriendlyName, exception);
		}
	}
}
