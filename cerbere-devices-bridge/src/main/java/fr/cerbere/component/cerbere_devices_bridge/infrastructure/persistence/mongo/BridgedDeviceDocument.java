package fr.cerbere.component.cerbere_devices_bridge.infrastructure.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Représentation Mongo d'un {@code BridgedDevice}. {@code type} et {@code state}
 * sont stockés en tant que noms d'énumération pour préserver la souplesse de schéma.
 * {@code version} (verrouillage optimiste Mongo) protège contre les écritures concurrentes.
 */
@Document(collection = "bridged_devices")
public record BridgedDeviceDocument(
	@Id String id,
	String type,
	String label,
	String zoneId,
	String state,
	@Version Long version
) {
}
