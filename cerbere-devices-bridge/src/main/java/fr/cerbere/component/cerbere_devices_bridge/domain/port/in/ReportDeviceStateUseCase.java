package fr.cerbere.component.cerbere_devices_bridge.domain.port.in;

import fr.cerbere.component.cerbere_devices_bridge.domain.event.DeviceEventOccurred;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceState;

import java.util.UUID;

/**
 * Port d'entrée : rapporter l'état réellement observé d'un device physique
 * (reçu via MQTT), seul chemin de publication d'un événement — appelé par
 * {@code ZigbeeDeviceStateMqttListener}.
 */
public interface ReportDeviceStateUseCase {

	DeviceEventOccurred report(UUID deviceId, DeviceState observedState);
}
