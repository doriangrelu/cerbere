package fr.cerbere.component.cerbere_core.domain.exception;

/**
 * Levée lorsqu'une zone est enregistrée ou renommée avec un nom déjà porté
 * par une autre zone du registre officiel.
 */
public final class DuplicateZoneNameException extends RuntimeException {

	public DuplicateZoneNameException(final String name) {
		super("A zone with this name already exists: " + name);
	}
}
