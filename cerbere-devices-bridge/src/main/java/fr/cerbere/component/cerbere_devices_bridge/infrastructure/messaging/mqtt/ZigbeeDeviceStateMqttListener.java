package fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.mqtt;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.BridgedDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.ContactState;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceState;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.MotionState;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.SirenState;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.ReportDeviceStateUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.BridgedDeviceRepository;
import fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.mqtt.payload.ContactSensorPayload;
import fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.mqtt.payload.MotionSensorPayload;
import fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.mqtt.payload.SwitchState;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

/**
 * Reçoit chaque message publié par Zigbee2MQTT sur {@code <base-topic>/+} (un
 * device par topic, voir docs/architecture/mqtt-zigbee-contracts.md). Le
 * {@code friendly_name} du topic EST l'UUID du device (voir ADR 0004/0016,
 * même principe de corrélation que `cerbere-devices-mock`) : lit
 * {@link BridgedDeviceRepository} directement (comme {@code DeviceSimulationScheduler}
 * le fait déjà côté mock) pour connaître le type avant de choisir le bon payload,
 * puis délègue à {@link ReportDeviceStateUseCase}. Résilient : device inconnu,
 * friendly_name non-UUID, ou payload illisible → log et ignore, jamais d'exception
 * qui romprait la connexion MQTT.
 */
@Component
public final class ZigbeeDeviceStateMqttListener implements MqttCallback {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZigbeeDeviceStateMqttListener.class);

	private final BridgedDeviceRepository bridgedDeviceRepository;
	private final ReportDeviceStateUseCase reportDeviceStateUseCase;
	private final ObjectMapper objectMapper;
	private final String baseTopic;

	public ZigbeeDeviceStateMqttListener(final BridgedDeviceRepository bridgedDeviceRepository,
										  final ReportDeviceStateUseCase reportDeviceStateUseCase,
										  @Qualifier("objectMapper") final ObjectMapper objectMapper,
										  @Value("${cerbere.devices-bridge.mqtt.base-topic}") final String baseTopic) {
		this.bridgedDeviceRepository = bridgedDeviceRepository;
		this.reportDeviceStateUseCase = reportDeviceStateUseCase;
		this.objectMapper = objectMapper;
		this.baseTopic = baseTopic;
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) {
		final String friendlyName = topic.substring(this.baseTopic.length() + 1);
		try {
			final UUID deviceId = UUID.fromString(friendlyName);
			final BridgedDevice device = this.bridgedDeviceRepository.findById(deviceId).orElse(null);
			if (device == null) {
				LOGGER.info("Ignoring MQTT message for unknown bridged device {}", friendlyName);
				return;
			}
			final DeviceState state = this.parseState(device.getType(), message.getPayload());
			this.reportDeviceStateUseCase.report(deviceId, state);
		} catch (final IllegalArgumentException exception) {
			LOGGER.info("Ignoring MQTT message on topic {}: friendly_name is not a Cerbère UUID", topic);
		} catch (final RuntimeException exception) {
			LOGGER.error("Failed to process MQTT message on topic {}: {}", topic, exception.getMessage());
		}
	}

	private DeviceState parseState(final DeviceType type, final byte[] payload) {
		return switch (type) {
			case CONTACT -> {
				final ContactSensorPayload parsed = this.objectMapper.readValue(payload, ContactSensorPayload.class);
				yield Boolean.TRUE.equals(parsed.contact()) ? ContactState.CLOSED : ContactState.OPEN;
			}
			case MOTION -> {
				final MotionSensorPayload parsed = this.objectMapper.readValue(payload, MotionSensorPayload.class);
				yield Boolean.TRUE.equals(parsed.occupancy()) ? MotionState.DETECTED : MotionState.CLEAR;
			}
			case SIREN -> {
				final SwitchState parsed = this.objectMapper.readValue(payload, SwitchState.class);
				yield SwitchState.ON.equals(parsed.state()) ? SirenState.ACTIVE : SirenState.INACTIVE;
			}
		};
	}

	@Override
	public void connectComplete(final boolean reconnect, final String serverURI) {
		LOGGER.info("MQTT connected to {} (reconnect={})", serverURI, reconnect);
	}

	@Override
	public void disconnected(final MqttDisconnectResponse disconnectResponse) {
		LOGGER.warn("MQTT disconnected: {}", disconnectResponse);
	}

	@Override
	public void mqttErrorOccurred(final MqttException exception) {
		LOGGER.error("MQTT error: {}", exception.getMessage());
	}

	@Override
	public void deliveryComplete(final IMqttToken token) {
		// no-op : ce listener ne publie pas lui-même, voir MqttCommandPublisher
	}

	@Override
	public void authPacketArrived(final int reasonCode, final MqttProperties properties) {
		// no-op : pas d'authentification renforcée configurée
	}
}
