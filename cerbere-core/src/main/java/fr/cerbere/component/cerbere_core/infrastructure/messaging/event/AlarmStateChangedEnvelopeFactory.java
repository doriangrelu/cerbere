package fr.cerbere.component.cerbere_core.infrastructure.messaging.event;

import fr.cerbere.component.cerbere_core.domain.event.AlarmStateChanged;
import fr.cerbere.shared.event.EventEnvelope;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Traduit un {@link AlarmStateChanged} (événement de domaine) vers l'{@link EventEnvelope}
 * commune, publiée sur {@code cerbere.alarm.state-changed}.
 */
public final class AlarmStateChangedEnvelopeFactory {

	private static final String SOURCE = "cerbere-core";

	private AlarmStateChangedEnvelopeFactory() {
	}

	public static EventEnvelope from(final AlarmStateChanged event) {
		final Map<String, Object> payload = Map.of(
			"previousMode", event.previousMode().name(),
			"newMode", event.newMode().name(),
			"triggered", event.triggered()
		);
		return new EventEnvelope(
			UUID.randomUUID(),
			"alarm.state_changed",
			event.occurredAt(),
			Instant.now(),
			SOURCE,
			null,
			null,
			event.correlationId(),
			payload
		);
	}
}
