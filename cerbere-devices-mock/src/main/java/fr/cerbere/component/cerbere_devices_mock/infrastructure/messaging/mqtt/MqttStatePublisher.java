package fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.mqtt;

import fr.cerbere.component.cerbere_devices_mock.domain.model.ContactState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_mock.domain.model.MotionState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SirenState;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.DeviceStatePublisher;
import fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.mqtt.payload.ContactSensorPayload;
import fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.mqtt.payload.MotionSensorPayload;
import fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.mqtt.payload.SwitchState;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Implémentation MQTT du port {@link DeviceStatePublisher} : publie sur
 * {@code <base-topic>/<friendlyName>} avec exactement les mêmes payloads qu'un
 * vrai device Zigbee2MQTT (voir docs/architecture/mqtt-zigbee-contracts.md et
 * ADR 0021), pour que {@code cerbere-devices-bridge} ne fasse aucune différence
 * entre le Mock et du matériel réel.
 */
@Component
public class MqttStatePublisher implements DeviceStatePublisher {

	private static final int QOS = 1;

	private final MqttClient mqttClient;
	private final ObjectMapper objectMapper;
	private final String baseTopic;

	public MqttStatePublisher(final MqttClient mqttClient,
							   @Qualifier("objectMapper") final ObjectMapper objectMapper,
							   @Value("${cerbere.devices-mock.mqtt.base-topic}") final String baseTopic) {
		this.mqttClient = mqttClient;
		this.objectMapper = objectMapper;
		this.baseTopic = baseTopic;
	}

	@Override
	public void publish(final String friendlyName, final DeviceType type, final DeviceState state) {
		final Object payload = this.payloadOf(type, state);
		try {
			final byte[] bytes = this.objectMapper.writeValueAsBytes(payload);
			final MqttMessage message = new MqttMessage(bytes);
			message.setQos(QOS);
			this.mqttClient.publish(this.baseTopic + "/" + friendlyName, message);
		} catch (final MqttException exception) {
			throw new IllegalStateException("Failed to publish MQTT state for device " + friendlyName, exception);
		}
	}

	private Object payloadOf(final DeviceType type, final DeviceState state) {
		return switch (type) {
			case CONTACT -> new ContactSensorPayload(state == ContactState.CLOSED, 100, 3025, 22.0);
			case MOTION -> new MotionSensorPayload(state == MotionState.DETECTED, 42, 100, 3000);
			case SIREN -> new SwitchState(state == SirenState.ACTIVE ? SwitchState.ON : SwitchState.OFF);
		};
	}
}
