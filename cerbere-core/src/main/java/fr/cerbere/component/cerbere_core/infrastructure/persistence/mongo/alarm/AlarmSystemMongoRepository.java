package fr.cerbere.component.cerbere_core.infrastructure.persistence.mongo.alarm;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository Spring Data, volontairement non public : seul {@link AlarmSystemMongoRepositoryAdapter}
 * (même package) doit en dépendre. Le reste de l'application passe par le port
 * {@link fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmSystemRepository}.
 */
interface AlarmSystemMongoRepository extends MongoRepository<AlarmSystemDocument, String> {
}
