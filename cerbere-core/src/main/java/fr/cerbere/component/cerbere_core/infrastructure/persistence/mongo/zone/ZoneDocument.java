package fr.cerbere.component.cerbere_core.infrastructure.persistence.mongo.zone;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Représentation Mongo d'une {@code Zone}. {@code version} (verrouillage
 * optimiste Mongo) protège contre les écritures concurrentes.
 */
@Document(collection = "zones")
public record ZoneDocument(
	@Id String id,
	String name,
	List<String> deviceIds,
	@Version Long version
) {
}
