package fr.cerbere.component.cerbere_history.infrastructure.messaging.event;

import fr.cerbere.component.cerbere_history.domain.model.DeviceEventRecord;
import fr.cerbere.shared.event.EventEnvelope;

import java.time.Instant;

/**
 * Traduit un {@link EventEnvelope} (module {@code cerbere-shared-kernel})
 * reçu sur {@code cerbere.device.events.raw} vers le modèle de domaine
 * {@link DeviceEventRecord}.
 */
public final class DeviceEventRecordFactory {

	private DeviceEventRecordFactory() {
	}

	public static DeviceEventRecord from(final EventEnvelope envelope) {
		return new DeviceEventRecord(
			envelope.eventId(),
			envelope.deviceId(),
			envelope.zoneId(),
			envelope.eventType(),
			envelope.payload(),
			envelope.occurredAt(),
			Instant.now(),
			envelope.correlationId()
		);
	}
}
