package fr.cerbere.component.cerbere_devices_bridge.adapter.config;

import fr.cerbere.shared.event.EventEnvelope;
import fr.cerbere.shared.kafka.JacksonEventDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.Map;

/**
 * Configuration explicite du consommateur Kafka typé {@link EventEnvelope}
 * (copie conforme de `cerbere-devices-mock`/`cerbere-core`).
 */
@EnableKafka
@Configuration(proxyBeanMethods = false)
public final class KafkaConsumerConfig {

	@Bean
	public ConsumerFactory<String, EventEnvelope> eventEnvelopeConsumerFactory(
			@Value("${spring.kafka.bootstrap-servers}") final String bootstrapServers,
			@Value("${spring.application.name}") final String applicationName) {
		final Map<String, Object> configuration = Map.of(
				ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
				ConsumerConfig.GROUP_ID_CONFIG, applicationName,
				ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
				ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonEventDeserializer.class,
				ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
		);
		return new DefaultKafkaConsumerFactory<>(configuration);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, EventEnvelope> eventEnvelopeKafkaListenerContainerFactory(
			final ConsumerFactory<String, EventEnvelope> eventEnvelopeConsumerFactory) {
		final ConcurrentKafkaListenerContainerFactory<String, EventEnvelope> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(eventEnvelopeConsumerFactory);
		factory.setAutoStartup(true);
		return factory;
	}
}
