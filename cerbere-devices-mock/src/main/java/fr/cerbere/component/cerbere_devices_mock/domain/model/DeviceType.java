package fr.cerbere.component.cerbere_devices_mock.domain.model;

/**
 * Type de device simulé pour la V1 de Cerbère : contact porte/fenêtre,
 * détecteur de mouvement, sirène. Chaque type sait quel {@link DeviceState}
 * il accepte, son état initial, et comment tirer/parser un état.
 */
public enum DeviceType {

	CONTACT(ContactState.CLOSED),
	MOTION(MotionState.CLEAR),
	SIREN(SirenState.INACTIVE);

	private final DeviceState initialState;

	DeviceType(final DeviceState initialState) {
		this.initialState = initialState;
	}

	public DeviceState initialState() {
		return this.initialState;
	}

	/**
	 * Indique si l'état donné appartient bien à la famille d'états de ce type de device.
	 */
	public boolean supports(final DeviceState state) {
		return switch (this) {
			case CONTACT -> state instanceof ContactState;
			case MOTION -> state instanceof MotionState;
			case SIREN -> state instanceof SirenState;
		};
	}

	/**
	 * Tire un état aléatoire compatible avec ce type, utilisé par le scheduler de simulation.
	 */
	public DeviceState randomState() {
		return switch (this) {
			case CONTACT -> ContactState.random();
			case MOTION -> MotionState.random();
			case SIREN -> SirenState.random();
		};
	}

	/**
	 * Parse le nom brut d'un état (ex: venant d'une requête REST ou d'un document Mongo)
	 * dans l'énumération d'état correspondant à ce type.
	 */
	public DeviceState parseState(final String rawState) {
		return switch (this) {
			case CONTACT -> ContactState.valueOf(rawState);
			case MOTION -> MotionState.valueOf(rawState);
			case SIREN -> SirenState.valueOf(rawState);
		};
	}
}
