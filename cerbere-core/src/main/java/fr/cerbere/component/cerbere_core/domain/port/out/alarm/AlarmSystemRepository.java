package fr.cerbere.component.cerbere_core.domain.port.out.alarm;

import fr.cerbere.component.cerbere_core.domain.model.AlarmSystem;

import java.util.Optional;

/**
 * Port de sortie : persistance de l'agrégat {@link AlarmSystem}. Système
 * mono-site : une seule instance persistée, identifiée par un {@code systemId}
 * métier stable (ex : {@code "home-1"}).
 */
public interface AlarmSystemRepository {

	AlarmSystem save(AlarmSystem alarmSystem);

	Optional<AlarmSystem> findById(String systemId);
}
