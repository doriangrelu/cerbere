package fr.cerbere.component.cerbere_devices_mock.domain.port.in;

import fr.cerbere.component.cerbere_devices_mock.domain.event.DeviceEventOccurred;

import java.util.UUID;

/**
 * Port d'entrée : déclencher un événement aléatoire sur un device simulé
 * (utilisé par le scheduler de simulation automatique).
 */
public interface SimulateDeviceEventUseCase {

	DeviceEventOccurred simulateRandomEvent(UUID deviceId);
}
