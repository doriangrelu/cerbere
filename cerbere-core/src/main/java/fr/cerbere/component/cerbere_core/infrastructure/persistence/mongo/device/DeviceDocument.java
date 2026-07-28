package fr.cerbere.component.cerbere_core.infrastructure.persistence.mongo.device;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

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
        boolean linked,
        Instant lastSeenAt,
        @Version Long version
) {
}
