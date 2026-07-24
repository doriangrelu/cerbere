package fr.cerbere.component.cerbere_devices_bridge.adapter.config;

import fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.mqtt.ZigbeeDeviceStateMqttListener;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Connexion au broker MQTT (Mosquitto, alimenté par Zigbee2MQTT) et abonnement
 * au topic générique {@code <base-topic>/+} (un niveau, capture exactement
 * {@code <base-topic>/<friendly_name>} sans redescendre sur {@code /set}/{@code /availability}).
 * {@code cleanStart=false} : conserve la session/les abonnements entre deux
 * redémarrages du bridge côté broker.
 */
@Configuration(proxyBeanMethods = false)
public final class MqttClientConfig {

	private static final int QOS = 1;

	@Bean(destroyMethod = "disconnect")
	public MqttClient mqttClient(@Value("${cerbere.devices-bridge.mqtt.broker-url}") final String brokerUrl,
								  @Value("${cerbere.devices-bridge.mqtt.client-id}") final String clientId,
								  @Value("${cerbere.devices-bridge.mqtt.base-topic}") final String baseTopic,
								  final ZigbeeDeviceStateMqttListener listener) throws MqttException {
		final MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
		client.setCallback(listener);

		final MqttConnectionOptions options = new MqttConnectionOptions();
		options.setAutomaticReconnect(true);
		options.setCleanStart(false);
		client.connect(options);

		client.subscribe(baseTopic + "/+", QOS);
		return client;
	}
}
