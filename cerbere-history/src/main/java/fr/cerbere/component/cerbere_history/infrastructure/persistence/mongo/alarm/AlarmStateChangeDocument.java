package fr.cerbere.component.cerbere_history.infrastructure.persistence.mongo.alarm;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Représentation Mongo d'un {@code AlarmStateChangeRecord}. {@code id} est
 * l'{@code eventId} d'origine, voir {@code DeviceEventDocument} pour le
 * principe d'idempotence.
 */
@Document(collection = "alarm_state_change_history")
public record AlarmStateChangeDocument(
	@Id String id,
	String previousMode,
	String newMode,
	boolean triggered,
	@Indexed Instant occurredAt,
	Instant recordedAt,
	String correlationId
) {
}
