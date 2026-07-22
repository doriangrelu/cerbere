package fr.cerbere.component.cerbere_core.domain.port.in.zone;

import fr.cerbere.component.cerbere_core.domain.exception.ZoneNotEmptyException;

import java.util.UUID;

/**
 * Port d'entrée : supprimer une zone.
 *
 * @throws ZoneNotEmptyException si des devices sont encore rattachés à la zone
 */
public interface DeleteZoneUseCase {

	void delete(UUID id);
}
