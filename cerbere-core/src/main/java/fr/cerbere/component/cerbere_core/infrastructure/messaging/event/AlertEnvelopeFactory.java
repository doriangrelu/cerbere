package fr.cerbere.component.cerbere_core.infrastructure.messaging.event;

import fr.cerbere.component.cerbere_core.domain.event.AlertRaised;
import fr.cerbere.shared.event.EventEnvelope;

import java.time.Instant;
import java.util.Map;

/**
 * Traduit un {@link AlertRaised} (événement de domaine) vers l'{@link EventEnvelope}
 * commune, publiée sur {@code cerbere.alarm.alerts}.
 */
public final class AlertEnvelopeFactory {

	private static final String SOURCE = "cerbere-core";

	private AlertEnvelopeFactory() {
	}

	public static EventEnvelope from(final AlertRaised event) {
		final Map<String, Object> payload = Map.of(
			"severity", event.severity().name(),
			"message", event.message()
		);
		return new EventEnvelope(
			event.alertId(),
			"alarm.alert_raised",
			event.occurredAt(),
			Instant.now(),
			SOURCE,
			event.deviceId(),
			event.zoneId(),
			event.correlationId(),
			payload
		);
	}
}
