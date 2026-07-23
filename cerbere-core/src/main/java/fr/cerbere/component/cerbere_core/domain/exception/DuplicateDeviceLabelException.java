package fr.cerbere.component.cerbere_core.domain.exception;

/**
 * Levée lorsqu'un device est enregistré ou renommé avec un libellé déjà porté
 * par un autre device du registre officiel.
 */
public final class DuplicateDeviceLabelException extends RuntimeException {

	public DuplicateDeviceLabelException(final String label) {
		super("A device with this label already exists: " + label);
	}
}
