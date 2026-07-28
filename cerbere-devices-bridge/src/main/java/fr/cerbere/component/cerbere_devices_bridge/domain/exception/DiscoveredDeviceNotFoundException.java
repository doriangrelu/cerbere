package fr.cerbere.component.cerbere_devices_bridge.domain.exception;

/**
 * Levée lorsqu'une demande d'appairage cible un {@code friendly_name} qui n'a
 * jamais été vu sur MQTT (device inconnu, ou déjà appairé depuis).
 */
public final class DiscoveredDeviceNotFoundException extends RuntimeException {

	public DiscoveredDeviceNotFoundException(final String friendlyName) {
		super("Discovered device not found: " + friendlyName);
	}
}
