package fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.kafka.producer;

import fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.event.DeviceEventEnvelope;
import org.apache.kafka.common.serialization.Serializer;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

/**
 * Sérialiseur Kafka écrit à la main pour {@link DeviceEventEnvelope}. Nécessaire
 * car {@code spring-kafka} (3.3.8) n'expose encore qu'un {@code JsonSerializer}
 * basé sur Jackson 2 ({@code com.fasterxml.jackson.databind}), absent du classpath
 * de ce module qui utilise Jackson 3 ({@code tools.jackson.databind}, défaut de
 * Spring Boot 4.1) — voir ADR 0012. Instancié par réflexion par Kafka via
 * {@code ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG}, d'où le constructeur
 * implicite sans argument.
 */
public final class DeviceEventEnvelopeSerializer implements Serializer<DeviceEventEnvelope> {

	private final JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();

	@Override
	public byte[] serialize(final String topic, final DeviceEventEnvelope data) {
		if (data == null) {
			return null;
		}
		return this.jsonMapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8);
	}
}
