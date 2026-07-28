package fr.cerbere.component.cerbere_devices_bridge.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository Spring Data : seul {@link DiscoveredDeviceMongoRepositoryAdapter}
 * (même package) doit en dépendre. Le reste de l'application passe par le port
 * {@link fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DiscoveredDeviceRepository}.
 */
public interface DiscoveredDeviceMongoRepository extends MongoRepository<DiscoveredDeviceDocument, String> {
}
