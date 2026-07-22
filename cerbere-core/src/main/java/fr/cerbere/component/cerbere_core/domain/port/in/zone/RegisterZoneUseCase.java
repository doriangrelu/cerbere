package fr.cerbere.component.cerbere_core.domain.port.in.zone;

import fr.cerbere.component.cerbere_core.domain.model.Zone;

/**
 * Port d'entrée : enregistrer une nouvelle zone.
 */
public interface RegisterZoneUseCase {

	Zone register(String name);
}
