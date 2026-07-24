package fr.cerbere.component.cerbere_devices_bridge.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository Spring Data, volontairement non exposé en dehors du package :
 * seul {@link BridgedDeviceMongoRepositoryAdapter} doit en dépendre. Le reste
 * de l'application passe par le port
 * {@link fr.cerbere.component.cerbere_devices_bridge.domain.port.out.BridgedDeviceRepository}.
 */
interface BridgedDeviceMongoRepository extends MongoRepository<BridgedDeviceDocument, String> {

	List<BridgedDeviceDocument> findByType(String type);
}
