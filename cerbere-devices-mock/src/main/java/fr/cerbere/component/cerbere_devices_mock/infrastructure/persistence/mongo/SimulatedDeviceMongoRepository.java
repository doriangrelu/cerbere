package fr.cerbere.component.cerbere_devices_mock.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data, volontairement non public : seul {@link SimulatedDeviceMongoRepositoryAdapter}
 * (même package) doit en dépendre. Le reste de l'application passe par le port
 * {@link fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository}.
 */
public interface SimulatedDeviceMongoRepository extends MongoRepository<SimulatedDeviceDocument, String> {

	List<SimulatedDeviceDocument> findByAutoSimulate(boolean autoSimulate);

	Optional<SimulatedDeviceDocument> findByFriendlyName(String friendlyName);
}
