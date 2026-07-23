package fr.cerbere.component.cerbere_core.infrastructure.persistence.mongo.zone;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository Spring Data, volontairement non public : seul {@link ZoneMongoRepositoryAdapter}
 * (même package) doit en dépendre. Le reste de l'application passe par le port
 * {@link fr.cerbere.component.cerbere_core.domain.port.out.zone.ZoneRepository}.
 */
interface ZoneMongoRepository extends MongoRepository<ZoneDocument, String> {

	Optional<ZoneDocument> findByName(String name);
}
