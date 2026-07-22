package fr.cerbere.component.cerbere_core.domain.exception;

/**
 * Levée lorsqu'un déclenchement est demandé alors que le système est désarmé.
 */
public final class AlarmNotArmedException extends RuntimeException {

	public AlarmNotArmedException(final String systemId) {
		super("Alarm system is disarmed, cannot trigger: " + systemId);
	}
}
