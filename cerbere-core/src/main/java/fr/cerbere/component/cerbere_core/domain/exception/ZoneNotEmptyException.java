package fr.cerbere.component.cerbere_core.domain.exception;

import java.util.UUID;

/**
 * Levée lorsqu'on tente de supprimer une zone à laquelle des devices sont
 * encore rattachés — il faut d'abord les réaffecter ou les supprimer.
 */
public final class ZoneNotEmptyException extends RuntimeException {

	public ZoneNotEmptyException(final UUID zoneId) {
		super("Zone still has devices assigned, cannot delete: " + zoneId);
	}
}
