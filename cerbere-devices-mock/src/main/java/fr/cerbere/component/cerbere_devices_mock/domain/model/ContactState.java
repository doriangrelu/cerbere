package fr.cerbere.component.cerbere_devices_mock.domain.model;

import java.util.Random;

/**
 * État d'un capteur de contact porte/fenêtre.
 */
public enum ContactState implements DeviceState {
	OPEN,
	CLOSED;

	private static final Random RANDOM = new Random();

	/**
	 * Tire un état aléatoire, utilisé par le scheduler de simulation.
	 */
	public static ContactState random() {
		return RANDOM.nextBoolean() ? OPEN : CLOSED;
	}
}
