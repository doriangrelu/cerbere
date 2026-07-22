package fr.cerbere.component.cerbere_devices_mock.domain.model;

import java.util.Random;

/**
 * État de l'actionneur sirène.
 */
public enum SirenState implements DeviceState {
	ACTIVE,
	INACTIVE;

	private static final Random RANDOM = new Random();

	/**
	 * Tire un état aléatoire, utilisé par le scheduler de simulation.
	 */
	public static SirenState random() {
		return RANDOM.nextBoolean() ? ACTIVE : INACTIVE;
	}
}
