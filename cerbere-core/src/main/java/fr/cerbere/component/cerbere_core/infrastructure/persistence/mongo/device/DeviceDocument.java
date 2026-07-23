package fr.cerbere.component.cerbere_core.infrastructure.persistence.mongo.device;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Représentation Mongo d'un {@code Device} du registre officiel. {@code version}
 * (verrouillage optimiste Mongo) protège contre les écritures concurrentes.
 */
@Document(collection = "devices")
public record DeviceDocument(
        @Id String id,
        String type,
        String label,
        String zoneId,
        boolean violation,
        boolean enabled,
        @Version Long version
) {
}
