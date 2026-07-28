package fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.mqtt;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.BridgedDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.ContactState;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceState;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.MotionState;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.SirenState;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.RecordDiscoveredDeviceUseCase;
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
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

/**
 * Reçoit chaque message publié par Zigbee2MQTT sur {@code <base-topic>/+} (un
 * device par topic, voir docs/architecture/mqtt-zigbee-contracts.md). Le
 * {@code friendly_name} du topic EST l'UUID du device une fois apparié (voir
 * ADR 0004/0016) : si le miroir local le connaît, l'état est rapporté via
 * {@link ReportDeviceStateUseCase}. Sinon, le device est enregistré comme
 * candidat à l'appairage ({@link RecordDiscoveredDeviceUseCase}, voir ADR 0023) —
 * un device qui parle sans être connu est précisément ce qu'on veut proposer à
 * l'écran Appairage, plutôt que de l'ignorer. Résilient : payload illisible ou
 * erreur inattendue → log et ignore, jamais d'exception qui romprait la
 * connexion MQTT.
 */
@Component
public final class ZigbeeDeviceStateMqttListener implements MqttCallback {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZigbeeDeviceStateMqttListener.class);

	/**
	 * Sous-topic réservé à l'API de la passerelle elle-même (état, logs, liste
	 * des devices) : jamais un device, ne doit pas polluer les candidats à l'appairage.
	 */
	private static final String BRIDGE_RESERVED_NAME = "bridge";

	private final BridgedDeviceRepository bridgedDeviceRepository;
	private final ReportDeviceStateUseCase reportDeviceStateUseCase;
	private final RecordDiscoveredDeviceUseCase recordDiscoveredDeviceUseCase;
	private final ObjectMapper objectMapper;
	private final String baseTopic;

	public ZigbeeDeviceStateMqttListener(final BridgedDeviceRepository bridgedDeviceRepository,
										  final ReportDeviceStateUseCase reportDeviceStateUseCase,
										  final RecordDiscoveredDeviceUseCase recordDiscoveredDeviceUseCase,
										  @Qualifier("objectMapper") final ObjectMapper objectMapper,
										  @Value("${cerbere.devices-bridge.mqtt.base-topic}") final String baseTopic) {
		this.bridgedDeviceRepository = bridgedDeviceRepository;
		this.reportDeviceStateUseCase = reportDeviceStateUseCase;
		this.recordDiscoveredDeviceUseCase = recordDiscoveredDeviceUseCase;
		this.objectMapper = objectMapper;
		this.baseTopic = baseTopic;
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) {
		final String friendlyName = topic.substring(this.baseTopic.length() + 1);
		if (BRIDGE_RESERVED_NAME.equals(friendlyName)) {
			return;
		}
		try {
			final BridgedDevice device = this.findBridgedDevice(friendlyName);
			if (device == null) {
				this.recordDiscoveredDeviceUseCase.record(friendlyName, this.inferType(message.getPayload()));
				return;
			}
			final DeviceState state = this.parseState(device.getType(), message.getPayload());
			this.reportDeviceStateUseCase.report(device.getId(), state);
		} catch (final RuntimeException exception) {
			LOGGER.error("Failed to process MQTT message on topic {}: {}", topic, exception.getMessage());
		}
	}

	/**
	 * Retrouve le device apparié correspondant au {@code friendly_name}, ou
	 * {@code null} si le nom n'est pas un UUID Cerbère ou n'est connu d'aucun miroir.
	 */
	private BridgedDevice findBridgedDevice(final String friendlyName) {
		final UUID deviceId;
		try {
			deviceId = UUID.fromString(friendlyName);
		} catch (final IllegalArgumentException exception) {
			return null;
		}
		return this.bridgedDeviceRepository.findById(deviceId).orElse(null);
	}

	/**
	 * Devine le type d'un device inconnu à la forme de son payload, pour aider
	 * l'usager à choisir la bonne cible d'appairage. {@code null} si indéterminable.
	 */
	private DeviceType inferType(final byte[] payload) {
		final Map<String, Object> fields = this.objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {
		});
		if (fields.containsKey("contact")) {
			return DeviceType.CONTACT;
		}
		if (fields.containsKey("occupancy")) {
			return DeviceType.MOTION;
		}
		if (fields.containsKey("state")) {
			return DeviceType.SIREN;
		}
		return null;
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
