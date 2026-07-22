package fr.cerbere.component.cerbere_devices_mock.domain.model;

/**
 * État courant d'un {@link SimulatedDevice}. Chaque {@link DeviceType} n'accepte
 * qu'une sous-famille d'états (voir {@link DeviceType#supports(DeviceState)}).
 */
public sealed interface DeviceState permits ContactState, MotionState, SirenState {

	/**
	 * Nom de la constante d'état, tel qu'utilisé pour la persistance et la sérialisation.
	 */
	String name();
}
