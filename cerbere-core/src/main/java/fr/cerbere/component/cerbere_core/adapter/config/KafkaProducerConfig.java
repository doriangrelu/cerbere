package fr.cerbere.component.cerbere_core.adapter.config;

import fr.cerbere.shared.event.EventEnvelope;
import fr.cerbere.shared.kafka.JacksonEventSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

/**
 * Configuration explicite du producteur Kafka typé {@link EventEnvelope}, partagée
 * par {@code AlarmStateChangedKafkaProducer} et {@code AlertKafkaProducer} (même
 * format d'enveloppe, topics différents choisis à l'envoi).
 */
@Configuration(proxyBeanMethods = false)
public final class KafkaProducerConfig {

	@Bean
	public ProducerFactory<String, EventEnvelope> eventEnvelopeProducerFactory(
			@Value("${spring.kafka.bootstrap-servers}") final String bootstrapServers) {
		final Map<String, Object> configuration = Map.of(
			ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
			ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
			ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonEventSerializer.class
		);
		return new DefaultKafkaProducerFactory<>(configuration);
	}

	@Bean
	public KafkaTemplate<String, EventEnvelope> eventEnvelopeKafkaTemplate(
			final ProducerFactory<String, EventEnvelope> eventEnvelopeProducerFactory) {
		return new KafkaTemplate<>(eventEnvelopeProducerFactory);
	}
}
