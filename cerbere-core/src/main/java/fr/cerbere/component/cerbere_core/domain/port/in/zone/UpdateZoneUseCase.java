package fr.cerbere.component.cerbere_core.domain.port.in.zone;

import fr.cerbere.component.cerbere_core.domain.model.Zone;

import java.util.UUID;

/**
 * Port d'entrée : renommer une zone existante.
 */
public interface UpdateZoneUseCase {

	Zone update(UUID id, String name);
}
