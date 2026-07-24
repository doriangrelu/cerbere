package fr.cerbere.component.cerbere_devices_bridge.application.usecase;

import fr.cerbere.component.cerbere_devices_bridge.domain.event.DeviceEventOccurred;
import fr.cerbere.component.cerbere_devices_bridge.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_bridge.domain.exception.UnsupportedDeviceStateException;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.BridgedDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.ContactState;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.MotionState;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.BridgedDeviceRepository;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DeviceEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires purs (aucun contexte Spring) du service central de report
 * d'état, exercé par le listener MQTT pour chaque message reçu.
 */
class ReportDeviceStateServiceTest {

	private InMemoryBridgedDeviceRepository repository;
	private RecordingDeviceEventPublisher publisher;
	private ReportDeviceStateService service;

	@BeforeEach
	void setUp() {
		this.repository = new InMemoryBridgedDeviceRepository();
		this.publisher = new RecordingDeviceEventPublisher();
		this.service = new ReportDeviceStateService(this.repository, this.publisher);
	}

	@Test
	void reportShouldPublishObservedStateAndPersistIt() {
		final UUID zoneId = UUID.randomUUID();
		final BridgedDevice device = this.repository.save(BridgedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte d'entrée", zoneId));

		final DeviceEventOccurred event = this.service.report(device.getId(), ContactState.OPEN);

		assertThat(event.newState()).isEqualTo(ContactState.OPEN);
		assertThat(event.deviceId()).isEqualTo(device.getId());
		assertThat(event.zoneId()).isEqualTo(zoneId);
		assertThat(event.deviceType()).isEqualTo(DeviceType.CONTACT);
		assertThat(this.publisher.publishedEvents()).containsExactly(event);
		assertThat(this.repository.findById(device.getId()).orElseThrow().getLastKnownState()).isEqualTo(ContactState.OPEN);
	}

	@Test
	void reportShouldThrowWhenDeviceDoesNotExist() {
		assertThatThrownBy(() -> this.service.report(UUID.randomUUID(), ContactState.OPEN))
				.isInstanceOf(DeviceNotFoundException.class);
	}

	@Test
	void reportShouldThrowWhenObservedStateDoesNotBelongToDeviceType() {
		final BridgedDevice device = this.repository.save(BridgedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte d'entrée", null));

		assertThatThrownBy(() -> this.service.report(device.getId(), MotionState.DETECTED))
				.isInstanceOf(UnsupportedDeviceStateException.class);
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

	private static final class RecordingDeviceEventPublisher implements DeviceEventPublisher {

		private final List<DeviceEventOccurred> events = new ArrayList<>();

		@Override
		public void publish(final DeviceEventOccurred event) {
			this.events.add(event);
		}

		List<DeviceEventOccurred> publishedEvents() {
			return this.events;
		}
	}
}
