package fr.cerbere.component.cerbere_history.infrastructure.persistence.mongo.device;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * Représentation Mongo d'un {@code DeviceEventRecord}. {@code id} est
 * l'{@code eventId} d'origine : un message Kafka rejoué écrase le même
 * document plutôt que d'en créer un doublon (idempotence, voir ADR — package
 * {@code application.service} n'est pas nécessaire ici, aucun invariant
 * inter-agrégat à maintenir).
 */
@Document(collection = "device_event_history")
public record DeviceEventDocument(
	@Id String id,
	@Indexed String deviceId,
	@Indexed String zoneId,
	@Indexed String eventType,
	Map<String, Object> payload,
	@Indexed Instant occurredAt,
	Instant recordedAt,
	String correlationId
) {
}
