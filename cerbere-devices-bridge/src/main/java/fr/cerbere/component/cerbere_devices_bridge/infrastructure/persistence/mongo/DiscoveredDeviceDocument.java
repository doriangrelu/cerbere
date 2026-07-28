package fr.cerbere.component.cerbere_devices_bridge.infrastructure.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Représentation Mongo d'un {@code DiscoveredDevice}. L'id Mongo est le
 * {@code friendlyName} lui-même : c'est la seule identité connue d'un device
 * pas encore appairé, et elle est unique côté Zigbee2MQTT — ça rend
 * l'enregistrement répété d'une même observation naturellement idempotent.
 * {@code version} (verrouillage optimiste Mongo) protège contre les écritures
 * concurrentes.
 */
@Document(collection = "discovered_devices")
public record DiscoveredDeviceDocument(
	@Id String friendlyName,
	String inferredType,
	Instant lastSeenAt,
	@Version Long version
) {
}
