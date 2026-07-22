package fr.cerbere.component.cerbere_devices_mock.domain.model;

import java.util.Random;

/**
 * État d'un détecteur de mouvement.
 */
public enum MotionState implements DeviceState {
	DETECTED,
	CLEAR;

	private static final Random RANDOM = new Random();

	/**
	 * Tire un état aléatoire, utilisé par le scheduler de simulation.
	 */
	public static MotionState random() {
		return RANDOM.nextBoolean() ? DETECTED : CLEAR;
	}
}
