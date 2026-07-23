package fr.cerbere.component.cerbere_core.infrastructure.persistence.mongo.zone;

import fr.cerbere.component.cerbere_core.domain.model.Zone;
import fr.cerbere.component.cerbere_core.domain.port.out.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation Mongo du port {@link ZoneRepository}.
 */
@Repository
@RequiredArgsConstructor
public class ZoneMongoRepositoryAdapter implements ZoneRepository {

	private final ZoneMongoRepository mongoRepository;
	private final ZoneMapper zoneMapper;

	@Override
	public Zone save(final Zone zone) {
		final ZoneDocument saved = this.mongoRepository.save(this.zoneMapper.toDocument(zone));
		return this.zoneMapper.toDomain(saved);
	}

	@Override
	public Optional<Zone> findById(final UUID id) {
		return this.mongoRepository.findById(id.toString()).map(this.zoneMapper::toDomain);
	}

	@Override
	public List<Zone> findAll() {
		return this.mongoRepository.findAll().stream()
			.map(this.zoneMapper::toDomain)
			.toList();
	}

	@Override
	public Optional<Zone> findByName(final String name) {
		return this.mongoRepository.findByName(name).map(this.zoneMapper::toDomain);
	}

	@Override
	public void deleteById(final UUID id) {
		this.mongoRepository.deleteById(id.toString());
	}
}
