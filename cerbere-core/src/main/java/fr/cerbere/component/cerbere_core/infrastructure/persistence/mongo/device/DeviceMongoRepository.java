package fr.cerbere.component.cerbere_core.infrastructure.persistence.mongo.device;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data, volontairement non public : seul {@link DeviceMongoRepositoryAdapter}
 * (même package) doit en dépendre. Le reste de l'application passe par le port
 * {@link fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository}.
 */
interface DeviceMongoRepository extends MongoRepository<DeviceDocument, String> {

	List<DeviceDocument> findByZoneId(String zoneId);

	Optional<DeviceDocument> findByLabel(String label);
}
