package fr.cerbere.component.cerbere_devices_bridge.domain.model;

/**
 * Type de device physique piloté par le bridge : contact porte/fenêtre,
 * détecteur de mouvement, sirène (relais Zigbee générique). Chaque type sait
 * quel {@link DeviceState} il accepte et son état initial.
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
	 * Parse le nom brut d'un état (ex : issu d'un payload MQTT traduit, ou d'un
	 * document Mongo) dans l'énumération d'état correspondant à ce type.
	 */
	public DeviceState parseState(final String rawState) {
		return switch (this) {
			case CONTACT -> ContactState.valueOf(rawState);
			case MOTION -> MotionState.valueOf(rawState);
			case SIREN -> SirenState.valueOf(rawState);
		};
	}
}
