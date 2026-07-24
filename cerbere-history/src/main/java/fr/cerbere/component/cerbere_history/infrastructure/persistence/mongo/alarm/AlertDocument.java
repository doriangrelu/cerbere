package fr.cerbere.component.cerbere_history.infrastructure.persistence.mongo.alarm;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Représentation Mongo d'un {@code AlertRecord}. {@code id} est l'{@code eventId}
 * d'origine, voir {@code DeviceEventDocument} pour le principe d'idempotence.
 */
@Document(collection = "alert_history")
public record AlertDocument(
	@Id String id,
	@Indexed String deviceId,
	@Indexed String zoneId,
	@Indexed String severity,
	String message,
	@Indexed Instant occurredAt,
	Instant recordedAt,
	String correlationId
) {
}
