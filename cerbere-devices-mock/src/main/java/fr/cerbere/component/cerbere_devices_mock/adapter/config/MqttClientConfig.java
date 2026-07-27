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
 * et abonnement à {@code <base-topic>/+/set} : le Mock ne publie que son propre
 * état (via {@code MqttStatePublisher}) et n'a besoin d'écouter que les
 * commandes qui lui sont adressées (voir ADR 0021). {@code cleanStart=false} :
 * conserve la session/les abonnements entre deux redémarrages du mock côté broker.
 */
@Configuration(proxyBeanMethods = false)
public final class MqttClientConfig {

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
		return client;
	}
}
