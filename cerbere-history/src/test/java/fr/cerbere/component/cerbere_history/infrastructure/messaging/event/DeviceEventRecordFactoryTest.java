package fr.cerbere.component.cerbere_history.infrastructure.messaging.event;

import fr.cerbere.component.cerbere_history.domain.model.DeviceEventRecord;
import fr.cerbere.shared.event.EventEnvelope;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires de la traduction {@link EventEnvelope} → {@link DeviceEventRecord}.
 */
class DeviceEventRecordFactoryTest {

	@Test
	void fromShouldMapContactEventEnvelope() {
		final UUID deviceId = UUID.randomUUID();
		final UUID zoneId = UUID.randomUUID();
		final UUID eventId = UUID.randomUUID();
		final UUID correlationId = UUID.randomUUID();
		final Instant occurredAt = Instant.now().minusSeconds(5);
		final EventEnvelope envelope = new EventEnvelope(
			eventId, "device.contact.state_changed", occurredAt, Instant.now(),
			"cerbere-devices-mock", deviceId, zoneId, correlationId, Map.of("state", "OPEN")
		);

		final DeviceEventRecord record = DeviceEventRecordFactory.from(envelope);

		assertThat(record.eventId()).isEqualTo(eventId);
		assertThat(record.deviceId()).isEqualTo(deviceId);
		assertThat(record.zoneId()).isEqualTo(zoneId);
		assertThat(record.eventType()).isEqualTo("device.contact.state_changed");
		assertThat(record.payload()).containsEntry("state", "OPEN");
		assertThat(record.occurredAt()).isEqualTo(occurredAt);
		assertThat(record.correlationId()).isEqualTo(correlationId);
		assertThat(record.recordedAt()).isNotNull();
	}

	@Test
	void fromShouldTolerateNullZoneId() {
		final EventEnvelope envelope = new EventEnvelope(
			UUID.randomUUID(), "device.motion.detected", Instant.now(), Instant.now(),
			"cerbere-devices-mock", UUID.randomUUID(), null, UUID.randomUUID(), Map.of("detected", true)
		);

		final DeviceEventRecord record = DeviceEventRecordFactory.from(envelope);

		assertThat(record.zoneId()).isNull();
		assertThat(record.payload()).containsEntry("detected", true);
	}
}
