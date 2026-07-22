package fr.cerbere.component.cerbere_core.infrastructure.persistence.mongo.alarm;

import fr.cerbere.component.cerbere_core.domain.model.AlarmSystem;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmSystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Implémentation Mongo du port {@link AlarmSystemRepository}.
 */
@Repository
@RequiredArgsConstructor
public class AlarmSystemMongoRepositoryAdapter implements AlarmSystemRepository {

	private final AlarmSystemMongoRepository mongoRepository;
	private final AlarmSystemMapper alarmSystemMapper;

	@Override
	public AlarmSystem save(final AlarmSystem alarmSystem) {
		final AlarmSystemDocument saved = this.mongoRepository.save(this.alarmSystemMapper.toDocument(alarmSystem));
		return this.alarmSystemMapper.toDomain(saved);
	}

	@Override
	public Optional<AlarmSystem> findById(final String systemId) {
		return this.mongoRepository.findById(systemId).map(this.alarmSystemMapper::toDomain);
	}
}
