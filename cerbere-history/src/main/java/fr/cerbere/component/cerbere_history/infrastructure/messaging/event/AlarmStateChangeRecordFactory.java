package fr.cerbere.component.cerbere_history.infrastructure.messaging.event;

import fr.cerbere.component.cerbere_history.domain.model.AlarmMode;
import fr.cerbere.component.cerbere_history.domain.model.AlarmStateChangeRecord;
import fr.cerbere.shared.event.EventEnvelope;

import java.time.Instant;

/**
 * Traduit un {@link EventEnvelope} reçu sur {@code cerbere.alarm.state-changed}
 * vers le modèle de domaine {@link AlarmStateChangeRecord}.
 */
public final class AlarmStateChangeRecordFactory {

	private AlarmStateChangeRecordFactory() {
	}

	public static AlarmStateChangeRecord from(final EventEnvelope envelope) {
		final AlarmMode previousMode = AlarmMode.valueOf(envelope.getPayloadValue("previousMode").orElseThrow());
		final AlarmMode newMode = AlarmMode.valueOf(envelope.getPayloadValue("newMode").orElseThrow());
		final boolean triggered = envelope.getPayloadValue("triggered", Boolean.class).orElse(false);
		return new AlarmStateChangeRecord(
			envelope.eventId(),
			previousMode,
			newMode,
			triggered,
			envelope.occurredAt(),
			Instant.now(),
			envelope.correlationId()
		);
	}
}
