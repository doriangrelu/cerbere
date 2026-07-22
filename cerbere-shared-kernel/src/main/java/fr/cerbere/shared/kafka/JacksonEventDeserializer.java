package fr.cerbere.shared.kafka;

import fr.cerbere.shared.event.EventEnvelope;
import org.apache.kafka.common.serialization.Deserializer;
import tools.jackson.databind.json.JsonMapper;

/**
 * Désérialiseur Kafka JSON générique basé sur Jackson 3, symétrique à
 * {@link JacksonEventSerializer}. Toujours typé vers {@link EventEnvelope} :
 * c'est le seul format de message échangé sur les topics Cerbère (voir
 * docs/best-practices/kafka-conventions.md).
 */
public final class JacksonEventDeserializer implements Deserializer<EventEnvelope> {

	private final JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();

	@Override
	public EventEnvelope deserialize(final String topic, final byte[] data) {
		if (data == null) {
			return null;
		}
		return this.jsonMapper.readValue(data, EventEnvelope.class);
	}
}
