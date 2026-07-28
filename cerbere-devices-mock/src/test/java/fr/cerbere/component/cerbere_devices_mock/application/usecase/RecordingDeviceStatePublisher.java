package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.DeviceStatePublisher;

import java.util.ArrayList;
import java.util.List;

/**
 * Doublure de test du port de publication MQTT, partagée par les tests de
 * use-cases du module : enregistre ce qui aurait été publié.
 */
final class RecordingDeviceStatePublisher implements DeviceStatePublisher {

	private final List<PublishedState> states = new ArrayList<>();

	@Override
	public void publish(final String friendlyName, final DeviceType type, final DeviceState state) {
		this.states.add(new PublishedState(friendlyName, type, state));
	}

	List<PublishedState> publishedStates() {
		return this.states;
	}
}
