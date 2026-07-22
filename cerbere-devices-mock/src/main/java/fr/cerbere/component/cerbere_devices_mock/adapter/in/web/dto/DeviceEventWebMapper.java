package fr.cerbere.component.cerbere_devices_mock.adapter.in.web.dto;

import fr.cerbere.component.cerbere_devices_mock.domain.event.DeviceEventOccurred;

/**
 * Traduction entre l'événement de domaine {@link DeviceEventOccurred} et le DTO REST.
 */
public final class DeviceEventWebMapper {

	private DeviceEventWebMapper() {
	}

	public static DeviceEventResponse toResponse(final DeviceEventOccurred event) {
		return new DeviceEventResponse(
			event.eventId().toString(),
			event.deviceId().toString(),
			event.zoneId() != null ? event.zoneId().toString() : null,
			event.deviceType().name(),
			event.newState().name(),
			event.occurredAt(),
			event.correlationId().toString(),
			event.triggeredManually()
		);
	}
}
