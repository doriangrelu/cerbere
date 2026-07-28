package fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.mqtt;

import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SirenState;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.RenameSimulatedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;
import fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.mqtt.payload.DeviceRenameRequest;
import fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.mqtt.payload.SwitchState;
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
 * Reçoit les messages adressés au Mock, qui joue à la fois le rôle du device
 * Zigbee et celui de la passerelle Zigbee2MQTT (voir ADR 0021/0023) :
 * <ul>
 *   <li>{@code <base-topic>/<friendlyName>/set} : commande de sirène envoyée par
 *   {@code cerbere-devices-bridge} — permet de vérifier que la commande arrive
 *   bien et produit le bon effet, comme sur du matériel réel ;</li>
 *   <li>{@code <base-topic>/bridge/request/device/rename} : requête d'appairage
 *   envoyée par le Bridge, traitée exactement comme le ferait la vraie
 *   passerelle (renommage du {@code friendly_name}).</li>
 * </ul>
 * Résilient : device inconnu ou payload illisible → log et ignore, jamais
 * d'exception qui romprait la connexion MQTT.
 */
@Component
public class MqttCommandListener implements MqttCallback {

	private static final Logger LOGGER = LoggerFactory.getLogger(MqttCommandListener.class);
	private static final String SET_SUFFIX = "/set";
	private static final String RENAME_TOPIC_SUFFIX = "/bridge/request/device/rename";

	private final SimulatedDeviceRepository simulatedDeviceRepository;
	private final RenameSimulatedDeviceUseCase renameSimulatedDeviceUseCase;
	private final ObjectMapper objectMapper;
	private final String baseTopic;

	public MqttCommandListener(final SimulatedDeviceRepository simulatedDeviceRepository,
							   final RenameSimulatedDeviceUseCase renameSimulatedDeviceUseCase,
							   @Qualifier("objectMapper") final ObjectMapper objectMapper,
							   @Value("${cerbere.devices-mock.mqtt.base-topic}") final String baseTopic) {
		this.simulatedDeviceRepository = simulatedDeviceRepository;
		this.renameSimulatedDeviceUseCase = renameSimulatedDeviceUseCase;
		this.objectMapper = objectMapper;
		this.baseTopic = baseTopic;
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) {
		try {
			if (topic.equals(this.baseTopic + RENAME_TOPIC_SUFFIX)) {
				this.handleRename(message);
				return;
			}
			this.handleSirenCommand(topic, message);
		} catch (final RuntimeException exception) {
			LOGGER.error("Failed to process MQTT message on topic {}: {}", topic, exception.getMessage());
		}
	}

	private void handleRename(final MqttMessage message) {
		final DeviceRenameRequest request = this.objectMapper.readValue(message.getPayload(), DeviceRenameRequest.class);
		final SimulatedDevice renamed = this.renameSimulatedDeviceUseCase.rename(request.from(), request.to());
		LOGGER.info("Simulated device renamed (paired): {} -> {}", request.from(), renamed.getFriendlyName());
	}

	private void handleSirenCommand(final String topic, final MqttMessage message) {
		final String withoutBase = topic.substring(this.baseTopic.length() + 1);
		final String friendlyName = withoutBase.substring(0, withoutBase.length() - SET_SUFFIX.length());
		final SimulatedDevice device = this.simulatedDeviceRepository.findByFriendlyName(friendlyName).orElse(null);
		if (device == null) {
			LOGGER.info("Ignoring MQTT command for unknown simulated device {}", friendlyName);
			return;
		}
		final SwitchState parsed = this.objectMapper.readValue(message.getPayload(), SwitchState.class);
		final SirenState newState = SwitchState.ON.equals(parsed.state()) ? SirenState.ACTIVE : SirenState.INACTIVE;
		this.simulatedDeviceRepository.save(device.withState(newState));
		LOGGER.info("Simulated device {} received command: {}", friendlyName, parsed.state());
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
