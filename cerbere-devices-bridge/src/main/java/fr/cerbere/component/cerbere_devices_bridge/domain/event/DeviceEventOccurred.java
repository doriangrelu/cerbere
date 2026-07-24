package fr.cerbere.component.cerbere_devices_bridge.domain.event;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceState;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine émis lorsqu'un device physique rapporte un changement
 * d'état réel via MQTT. Contrairement à l'équivalent de {@code cerbere-devices-mock},
 * pas de distinction manuel/simulé : tout événement ici reflète une observation réelle.
 */
public record DeviceEventOccurred(
	UUID eventId,
	UUID deviceId,
	UUID zoneId,
	DeviceType deviceType,
	DeviceState newState,
	Instant occurredAt,
	UUID correlationId
) {
}
