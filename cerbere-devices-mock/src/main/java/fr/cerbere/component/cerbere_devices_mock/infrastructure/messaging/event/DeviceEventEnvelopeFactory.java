package fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.event;

import fr.cerbere.component.cerbere_devices_mock.domain.event.DeviceEventOccurred;
import fr.cerbere.component.cerbere_devices_mock.domain.model.ContactState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.MotionState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SirenState;
import fr.cerbere.shared.event.EventEnvelope;

import java.time.Instant;
import java.util.Map;

/**
 * Traduit un {@link DeviceEventOccurred} (événement de domaine) vers l'{@link EventEnvelope}
 * commune (module {@code cerbere-shared-kernel}, voir ADR 0013). Le mapping
 * {@code eventType}/{@code payload} par type de device est documenté dans
 * docs/best-practices/kafka-conventions.md.
 */
public final class DeviceEventEnvelopeFactory {

	private static final String SOURCE = "cerbere-devices-mock";

	private DeviceEventEnvelopeFactory() {
	}

	public static EventEnvelope from(final DeviceEventOccurred event) {
		return new EventEnvelope(
			event.eventId(),
			eventTypeOf(event),
			event.occurredAt(),
			Instant.now(),
			SOURCE,
			event.deviceId(),
			event.zoneId(),
			event.correlationId(),
			payloadOf(event)
		);
	}

	private static String eventTypeOf(final DeviceEventOccurred event) {
		return switch (event.deviceType()) {
			case CONTACT -> "device.contact.state_changed";
			case MOTION -> "device.motion.detected";
			case SIREN -> "device.siren.triggered";
		};
	}

	private static Map<String, Object> payloadOf(final DeviceEventOccurred event) {
		return switch (event.newState()) {
			case ContactState contactState -> Map.of("state", contactState.name());
			case MotionState motionState -> Map.of("detected", motionState == MotionState.DETECTED);
			case SirenState sirenState -> Map.of("active", sirenState == SirenState.ACTIVE);
		};
	}
}
