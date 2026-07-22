package fr.cerbere.component.cerbere_devices_mock.domain.port.in;

import fr.cerbere.component.cerbere_devices_mock.domain.event.DeviceEventOccurred;

import java.util.UUID;

/**
 * Port d'entrée : déclencher manuellement un événement précis sur un device
 * simulé (API de contrôle, usage démo/QA).
 */
public interface TriggerDeviceEventUseCase {

	DeviceEventOccurred trigger(UUID deviceId, String requestedStateName);
}
