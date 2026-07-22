package fr.cerbere.component.cerbere_core.infrastructure.persistence.mongo.device;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Représentation Mongo d'un {@code Device} du registre officiel.
 */
@Document(collection = "devices")
public record DeviceDocument(
        @Id String id,
        String type,
        String label,
        String zoneId,
        boolean violation,
        boolean enabled
) {
}
