package fr.cerbere.component.cerbere_history.infrastructure.messaging.event;

import fr.cerbere.component.cerbere_history.domain.model.AlertRecord;
import fr.cerbere.component.cerbere_history.domain.model.AlertSeverity;
import fr.cerbere.shared.event.EventEnvelope;

import java.time.Instant;

/**
 * Traduit un {@link EventEnvelope} reçu sur {@code cerbere.alarm.alerts}
 * vers le modèle de domaine {@link AlertRecord}.
 */
public final class AlertRecordFactory {

	private AlertRecordFactory() {
	}

	public static AlertRecord from(final EventEnvelope envelope) {
		final AlertSeverity severity = AlertSeverity.valueOf(envelope.getPayloadValue("severity").orElseThrow());
		final String message = envelope.getPayloadValue("message").orElse(null);
		return new AlertRecord(
			envelope.eventId(),
			envelope.deviceId(),
			envelope.zoneId(),
			severity,
			message,
			envelope.occurredAt(),
			Instant.now(),
			envelope.correlationId()
		);
	}
}
