package fr.cerbere.component.cerbere_devices_bridge.adapter.config;

import fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.kafka.producer.DeviceEventKafkaProducer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.time.Duration;

/**
 * Provisionnement explicite du topic {@code cerbere.device.events.raw}. Le
 * bridge en est désormais le seul producteur réel (voir ADR 0021 :
 * {@code cerbere-devices-mock} publie sur MQTT, plus directement sur Kafka),
 * donc son propriétaire — auparavant {@code cerbere-devices-mock}
 * (voir ADR 0014). Voir docs/best-practices/kafka-conventions.md pour la
 * convention de dimensionnement.
 */
@Configuration(proxyBeanMethods = false)
public final class KafkaTopicConfig {

	private static final int PARTITIONS = 3;
	private static final int REPLICAS = 1;
	private static final long RETENTION = Duration.ofDays(3).toMillis();

	@Bean
	public NewTopic deviceEventsRawTopic() {
		return TopicBuilder.name(DeviceEventKafkaProducer.TOPIC)
			.partitions(PARTITIONS)
			.replicas(REPLICAS)
			.config(TopicConfig.RETENTION_MS_CONFIG, Long.toString(RETENTION))
			.build();
	}
}
