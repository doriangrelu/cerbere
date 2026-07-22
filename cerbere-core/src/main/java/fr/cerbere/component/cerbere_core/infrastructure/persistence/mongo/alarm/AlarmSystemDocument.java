package fr.cerbere.component.cerbere_core.infrastructure.persistence.mongo.alarm;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Représentation Mongo de l'agrégat {@code AlarmSystem}.
 */
@Document(collection = "alarm_system")
public record AlarmSystemDocument(
	@Id String id,
	String mode,
	boolean triggered
) {
}
