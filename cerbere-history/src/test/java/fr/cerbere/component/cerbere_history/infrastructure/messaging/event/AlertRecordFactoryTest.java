package fr.cerbere.component.cerbere_history.infrastructure.messaging.event;

import fr.cerbere.component.cerbere_history.domain.model.AlertRecord;
import fr.cerbere.component.cerbere_history.domain.model.AlertSeverity;
import fr.cerbere.shared.event.EventEnvelope;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires de la traduction {@link EventEnvelope} → {@link AlertRecord}.
 */
class AlertRecordFactoryTest {

	@Test
	void fromShouldMapSeverityAndMessage() {
		final UUID eventId = UUID.randomUUID();
		final UUID deviceId = UUID.randomUUID();
		final UUID zoneId = UUID.randomUUID();
		final UUID correlationId = UUID.randomUUID();
		final EventEnvelope envelope = new EventEnvelope(
			eventId, "alarm.alert_raised", Instant.now(), Instant.now(), "cerbere-core",
			deviceId, zoneId, correlationId,
			Map.of("severity", "CRITICAL", "message", "Violation detected on Porte d'entrée")
		);

		final AlertRecord record = AlertRecordFactory.from(envelope);

		assertThat(record.eventId()).isEqualTo(eventId);
		assertThat(record.deviceId()).isEqualTo(deviceId);
		assertThat(record.zoneId()).isEqualTo(zoneId);
		assertThat(record.severity()).isEqualTo(AlertSeverity.CRITICAL);
		assertThat(record.message()).isEqualTo("Violation detected on Porte d'entrée");
		assertThat(record.correlationId()).isEqualTo(correlationId);
	}
}
