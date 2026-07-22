package fr.cerbere.shared.kafka;

import org.apache.kafka.common.serialization.Serializer;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

/**
 * Sérialiseur Kafka JSON générique basé sur Jackson 3 ({@code tools.jackson.databind}),
 * réutilisable par tous les producteurs Cerbère. Nécessaire car {@code spring-kafka}
 * (3.3.8 au moment de l'écriture) n'expose encore qu'un {@code JsonSerializer}
 * basé sur Jackson 2, absent du classpath des modules qui utilisent Jackson 3
 * (défaut de Spring Boot 4.1) — voir ADR 0012 et ADR 0013.
 *
 * <p>Fonctionne pour n'importe quel type d'événement (générique par effacement de
 * type) : Kafka instancie cette classe par réflexion via
 * {@code ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG}, d'où le constructeur
 * implicite sans argument.
 */
public final class JacksonEventSerializer implements Serializer<Object> {

	private final JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();

	@Override
	public byte[] serialize(final String topic, final Object data) {
		if (data == null) {
			return null;
		}
		return this.jsonMapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8);
	}
}
