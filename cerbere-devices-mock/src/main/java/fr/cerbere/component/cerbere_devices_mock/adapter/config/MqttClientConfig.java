package fr.cerbere.component.cerbere_devices_mock.adapter.config;

import fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.mqtt.MqttCommandListener;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Connexion au broker MQTT (le même Mosquitto que {@code cerbere-devices-bridge})
 * et deux abonnements, correspondant aux deux rôles que joue le Mock (voir
 * ADR 0021/0023) : {@code <base-topic>/+/set} pour les commandes adressées au
 * device lui-même, et {@code <base-topic>/bridge/request/device/rename} pour les
 * requêtes d'appairage, que la vraie passerelle Zigbee2MQTT traiterait.
 * {@code cleanStart=false} : conserve la session/les abonnements entre deux
 * redémarrages du mock côté broker.
 */
@Configuration(proxyBeanMethods = false)
public class MqttClientConfig {

	private static final int QOS = 1;

	@Bean(destroyMethod = "disconnect")
	public MqttClient mqttClient(@Value("${cerbere.devices-mock.mqtt.broker-url}") final String brokerUrl,
								  @Value("${cerbere.devices-mock.mqtt.client-id}") final String clientId,
								  @Value("${cerbere.devices-mock.mqtt.base-topic}") final String baseTopic,
								  final MqttCommandListener listener) throws MqttException {
		final MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
		client.setCallback(listener);

		final MqttConnectionOptions options = new MqttConnectionOptions();
		options.setAutomaticReconnect(true);
		options.setCleanStart(false);
		client.connect(options);

		client.subscribe(baseTopic + "/+/set", QOS);
		client.subscribe(baseTopic + "/bridge/request/device/rename", QOS);
		return client;
	}
}
