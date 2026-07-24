package fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.mqtt;

import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DeviceCommandPublisher;
import fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.mqtt.payload.SwitchState;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

/**
 * Implémentation MQTT du port {@link DeviceCommandPublisher}. Publie sur
 * {@code <base-topic>/<deviceId>/set} — voir docs/architecture/mqtt-zigbee-contracts.md.
 */
@Component
public final class MqttCommandPublisher implements DeviceCommandPublisher {

	private final MqttClient mqttClient;
	private final ObjectMapper objectMapper;
	private final String baseTopic;

	public MqttCommandPublisher(final MqttClient mqttClient,
								 @Qualifier("objectMapper") final ObjectMapper objectMapper,
								 @Value("${cerbere.devices-bridge.mqtt.base-topic}") final String baseTopic) {
		this.mqttClient = mqttClient;
		this.objectMapper = objectMapper;
		this.baseTopic = baseTopic;
	}

	@Override
	public void switchOn(final UUID deviceId) {
		this.publish(deviceId, SwitchState.ON);
	}

	@Override
	public void switchOff(final UUID deviceId) {
		this.publish(deviceId, SwitchState.OFF);
	}

	private void publish(final UUID deviceId, final String state) {
		try {
			final byte[] payload = this.objectMapper.writeValueAsBytes(new SwitchState(state));
			final MqttMessage message = new MqttMessage(payload);
			message.setQos(1);
			this.mqttClient.publish(this.baseTopic + "/" + deviceId + "/set", message);
		} catch (final MqttException exception) {
			throw new IllegalStateException("Failed to publish MQTT command for device " + deviceId, exception);
		}
	}
}
