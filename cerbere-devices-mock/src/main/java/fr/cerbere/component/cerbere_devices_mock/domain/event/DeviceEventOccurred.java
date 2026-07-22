package fr.cerbere.component.cerbere_devices_mock.domain.event;

import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine émis lorsqu'un device simulé change d'état, que ce soit
 * via le scheduler automatique ou l'API de déclenchement manuel.
 */
public record DeviceEventOccurred(
	UUID eventId,
	UUID deviceId,
	UUID zoneId,
	DeviceType deviceType,
	DeviceState newState,
	Instant occurredAt,
	UUID correlationId,
	boolean triggeredManually
) {
}
