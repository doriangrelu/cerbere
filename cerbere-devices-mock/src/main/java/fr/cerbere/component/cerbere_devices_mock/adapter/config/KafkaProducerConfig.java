package fr.cerbere.component.cerbere_devices_mock.adapter.config;

import fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.event.DeviceEventEnvelope;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.kafka.producer.DeviceEventEnvelopeSerializer;

import java.util.Map;

/**
 * Configuration explicite du producteur Kafka typé {@link DeviceEventEnvelope},
 * plutôt que de dépendre de l'auto-configuration générique de Spring Boot.
 */
@Configuration(proxyBeanMethods = false)
public final class KafkaProducerConfig {

	@Bean
	public ProducerFactory<String, DeviceEventEnvelope> deviceEventProducerFactory(
			@Value("${spring.kafka.bootstrap-servers}") final String bootstrapServers) {
		final Map<String, Object> configuration = Map.of(
			ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
			ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
			ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DeviceEventEnvelopeSerializer.class
		);
		return new DefaultKafkaProducerFactory<>(configuration);
	}

	@Bean
	public KafkaTemplate<String, DeviceEventEnvelope> deviceEventKafkaTemplate(
			final ProducerFactory<String, DeviceEventEnvelope> deviceEventProducerFactory) {
		return new KafkaTemplate<>(deviceEventProducerFactory);
	}
}
