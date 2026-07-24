package fr.cerbere.component.cerbere_devices_bridge.application.usecase;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.BridgedDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.BridgedDeviceRepository;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DeviceCommandPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires purs (aucun contexte Spring) du pilotage des sirènes
 * physiques, exercé par le consommateur Kafka de {@code cerbere.alarm.state-changed}.
 */
class CommandSirenServiceTest {

	private InMemoryBridgedDeviceRepository repository;
	private RecordingDeviceCommandPublisher publisher;
	private CommandSirenService service;

	@BeforeEach
	void setUp() {
		this.repository = new InMemoryBridgedDeviceRepository();
		this.publisher = new RecordingDeviceCommandPublisher();
		this.service = new CommandSirenService(this.repository, this.publisher);
	}

	@Test
	void applyAlarmTriggeredShouldSwitchOnEverySirenWhenTriggered() {
		final BridgedDevice siren = this.repository.save(BridgedDevice.register(UUID.randomUUID(), DeviceType.SIREN, "Sirène garage", null));
		this.repository.save(BridgedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte d'entrée", null));

		this.service.applyAlarmTriggered(true);

		assertThat(this.publisher.switchedOn()).containsExactly(siren.getId());
		assertThat(this.publisher.switchedOff()).isEmpty();
	}

	@Test
	void applyAlarmTriggeredShouldSwitchOffEverySirenWhenNotTriggered() {
		final BridgedDevice siren = this.repository.save(BridgedDevice.register(UUID.randomUUID(), DeviceType.SIREN, "Sirène garage", null));

		this.service.applyAlarmTriggered(false);

		assertThat(this.publisher.switchedOff()).containsExactly(siren.getId());
		assertThat(this.publisher.switchedOn()).isEmpty();
	}

	@Test
	void applyAlarmTriggeredShouldDoNothingWhenNoSirenKnown() {
		this.repository.save(BridgedDevice.register(UUID.randomUUID(), DeviceType.MOTION, "Salon", null));

		this.service.applyAlarmTriggered(true);

		assertThat(this.publisher.switchedOn()).isEmpty();
		assertThat(this.publisher.switchedOff()).isEmpty();
	}

	private static final class InMemoryBridgedDeviceRepository implements BridgedDeviceRepository {

		private final Map<UUID, BridgedDevice> devices = new HashMap<>();

		@Override
		public BridgedDevice save(final BridgedDevice device) {
			this.devices.put(device.getId(), device);
			return device;
		}

		@Override
		public Optional<BridgedDevice> findById(final UUID id) {
			return Optional.ofNullable(this.devices.get(id));
		}

		@Override
		public List<BridgedDevice> findAll() {
			return List.copyOf(this.devices.values());
		}

		@Override
		public List<BridgedDevice> findByType(final DeviceType type) {
			return this.devices.values().stream().filter(device -> device.getType() == type).toList();
		}

		@Override
		public void deleteById(final UUID id) {
			this.devices.remove(id);
		}
	}

	private static final class RecordingDeviceCommandPublisher implements DeviceCommandPublisher {

		private final List<UUID> switchedOn = new ArrayList<>();
		private final List<UUID> switchedOff = new ArrayList<>();

		@Override
		public void switchOn(final UUID deviceId) {
			this.switchedOn.add(deviceId);
		}

		@Override
		public void switchOff(final UUID deviceId) {
			this.switchedOff.add(deviceId);
		}

		List<UUID> switchedOn() {
			return this.switchedOn;
		}

		List<UUID> switchedOff() {
			return this.switchedOff;
		}
	}
}
