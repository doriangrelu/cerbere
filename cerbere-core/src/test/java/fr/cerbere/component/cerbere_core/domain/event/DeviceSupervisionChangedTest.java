package fr.cerbere.component.cerbere_core.domain.event;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceSupervisionChangedTest {

	@Test
	void forDeviceShouldDropNullZoneIds() {
		final UUID deviceId = UUID.randomUUID();

		final DeviceSupervisionChanged event = DeviceSupervisionChanged.forDevice(deviceId, (UUID) null);

		assertThat(event.affectedZoneIds()).isEmpty();
	}

	@Test
	void forDeviceShouldKeepAMixOfNullAndRealZoneIds() {
		final UUID deviceId = UUID.randomUUID();
		final UUID zoneId = UUID.randomUUID();

		final DeviceSupervisionChanged event = DeviceSupervisionChanged.forDevice(deviceId, null, zoneId, null);

		assertThat(event.affectedZoneIds()).containsExactly(zoneId);
	}

	@Test
	void forDeviceShouldDeduplicateRepeatedZoneIds() {
		final UUID deviceId = UUID.randomUUID();
		final UUID zoneId = UUID.randomUUID();

		final DeviceSupervisionChanged event = DeviceSupervisionChanged.forDevice(deviceId, zoneId, zoneId);

		assertThat(event.affectedZoneIds()).containsExactly(zoneId);
	}

	@Test
	void constructorShouldToleratePlainNullZoneIdSet() {
		final DeviceSupervisionChanged event = new DeviceSupervisionChanged(UUID.randomUUID(), null);

		assertThat(event.affectedZoneIds()).isEmpty();
	}
}
