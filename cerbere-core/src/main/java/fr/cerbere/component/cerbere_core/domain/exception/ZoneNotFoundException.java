package fr.cerbere.component.cerbere_core.domain.exception;

import java.util.UUID;

/**
 * Levée lorsqu'une zone demandée n'existe pas.
 */
public final class ZoneNotFoundException extends RuntimeException {

	public ZoneNotFoundException(final UUID zoneId) {
		super("Zone not found: " + zoneId);
	}
}
