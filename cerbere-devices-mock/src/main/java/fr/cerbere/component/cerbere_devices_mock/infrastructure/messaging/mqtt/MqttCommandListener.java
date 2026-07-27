package fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.mqtt;

import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SirenState;
import fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.mqtt.payload.SwitchState;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;
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

/**
 * Reçoit les commandes publiées par {@code cerbere-devices-bridge} sur
 * {@code <base-topic>/<friendlyName>/set} (voir docs/architecture/mqtt-zigbee-contracts.md
 * et ADR 0021) — le Mock joue ici le rôle du relais Zigbee générique qui pilote
 * la sirène, exactement comme le ferait du matériel réel, ce qui permet de
 * vérifier que la commande envoyée par le Bridge arrive bien et produit le bon
 * effet. Résilient : device inconnu ou payload illisible → log et ignore,
 * jamais d'exception qui romprait la connexion MQTT.
 */
@Component
public final class MqttCommandListener implements MqttCallback {

	private static final Logger LOGGER = LoggerFactory.getLogger(MqttCommandListener.class);
	private static final String SET_SUFFIX = "/set";

	private final SimulatedDeviceRepository simulatedDeviceRepository;
	private final ObjectMapper objectMapper;
	private final String baseTopic;

	public MqttCommandListener(final SimulatedDeviceRepository simulatedDeviceRepository,
							   @Qualifier("objectMapper") final ObjectMapper objectMapper,
							   @Value("${cerbere.devices-mock.mqtt.base-topic}") final String baseTopic) {
		this.simulatedDeviceRepository = simulatedDeviceRepository;
		this.objectMapper = objectMapper;
		this.baseTopic = baseTopic;
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) {
		final String withoutBase = topic.substring(this.baseTopic.length() + 1);
		final String friendlyName = withoutBase.substring(0, withoutBase.length() - SET_SUFFIX.length());
		try {
			final SimulatedDevice device = this.simulatedDeviceRepository.findByFriendlyName(friendlyName).orElse(null);
			if (device == null) {
				LOGGER.info("Ignoring MQTT command for unknown simulated device {}", friendlyName);
				return;
			}
			final SwitchState parsed = this.objectMapper.readValue(message.getPayload(), SwitchState.class);
			final SirenState newState = SwitchState.ON.equals(parsed.state()) ? SirenState.ACTIVE : SirenState.INACTIVE;
			this.simulatedDeviceRepository.save(device.withState(newState));
			LOGGER.info("Simulated device {} received command: {}", friendlyName, parsed.state());
		} catch (final RuntimeException exception) {
			LOGGER.error("Failed to process MQTT command on topic {}: {}", topic, exception.getMessage());
		}
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
		// no-op : ce listener ne publie pas lui-même, voir MqttStatePublisher
	}

	@Override
	public void authPacketArrived(final int reasonCode, final MqttProperties properties) {
		// no-op : pas d'authentification renforcée configurée
	}
}
