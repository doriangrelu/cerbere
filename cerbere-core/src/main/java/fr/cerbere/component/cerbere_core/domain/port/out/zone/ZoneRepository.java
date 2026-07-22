package fr.cerbere.component.cerbere_core.domain.port.out.zone;

import fr.cerbere.component.cerbere_core.domain.model.Zone;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie : persistance des zones.
 */
public interface ZoneRepository {

	Zone save(Zone zone);

	Optional<Zone> findById(UUID id);

	List<Zone> findAll();

	void deleteById(UUID id);
}
