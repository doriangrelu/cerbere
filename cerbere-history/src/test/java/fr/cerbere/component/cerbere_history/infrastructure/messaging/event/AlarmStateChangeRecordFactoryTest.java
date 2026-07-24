package fr.cerbere.component.cerbere_history.infrastructure.messaging.event;

import fr.cerbere.component.cerbere_history.domain.model.AlarmMode;
import fr.cerbere.component.cerbere_history.domain.model.AlarmStateChangeRecord;
import fr.cerbere.shared.event.EventEnvelope;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires de la traduction {@link EventEnvelope} → {@link AlarmStateChangeRecord}.
 */
class AlarmStateChangeRecordFactoryTest {

	@Test
	void fromShouldMapModesAndTriggeredFlag() {
		final UUID eventId = UUID.randomUUID();
		final UUID correlationId = UUID.randomUUID();
		final Instant occurredAt = Instant.now().minusSeconds(2);
		final EventEnvelope envelope = new EventEnvelope(
			eventId, "alarm.state_changed", occurredAt, Instant.now(), "cerbere-core",
			null, null, correlationId,
			Map.of("previousMode", "DISARMED", "newMode", "ARMED_AWAY", "triggered", true)
		);

		final AlarmStateChangeRecord record = AlarmStateChangeRecordFactory.from(envelope);

		assertThat(record.eventId()).isEqualTo(eventId);
		assertThat(record.previousMode()).isEqualTo(AlarmMode.DISARMED);
		assertThat(record.newMode()).isEqualTo(AlarmMode.ARMED_AWAY);
		assertThat(record.triggered()).isTrue();
		assertThat(record.occurredAt()).isEqualTo(occurredAt);
		assertThat(record.correlationId()).isEqualTo(correlationId);
	}

	@Test
	void fromShouldDefaultTriggeredToFalseWhenAbsent() {
		final EventEnvelope envelope = new EventEnvelope(
			UUID.randomUUID(), "alarm.state_changed", Instant.now(), Instant.now(), "cerbere-core",
			null, null, UUID.randomUUID(),
			Map.of("previousMode", "ARMED_HOME", "newMode", "DISARMED")
		);

		final AlarmStateChangeRecord record = AlarmStateChangeRecordFactory.from(envelope);

		assertThat(record.triggered()).isFalse();
	}
}
