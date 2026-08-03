package fr.cerbere.component.cerbere_core.application.usecase.device;

import fr.cerbere.component.cerbere_core.domain.event.DeviceCreated;
import fr.cerbere.component.cerbere_core.domain.event.DeviceDeleted;
import fr.cerbere.component.cerbere_core.domain.event.DeviceSupervisionChanged;
import fr.cerbere.component.cerbere_core.domain.event.DeviceUpdated;
import fr.cerbere.component.cerbere_core.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_core.domain.exception.DuplicateDeviceLabelException;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.DeviceType;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DevicePublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceSupervisionChangedPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateDeviceServiceTest {

	private InMemoryDeviceRepository deviceRepository;
	private RecordingDevicePublisher publisher;
	private RecordingSupervisionChangedPublisher supervisionChangedPublisher;
	private UpdateDeviceService service;

	@BeforeEach
	void setUp() {
		this.deviceRepository = new InMemoryDeviceRepository();
		this.publisher = new RecordingDevicePublisher();
		this.supervisionChangedPublisher = new RecordingSupervisionChangedPublisher();
		this.service = new UpdateDeviceService(this.deviceRepository, this.publisher, this.supervisionChangedPublisher);
	}

	@Test
	void updateShouldRejectUnknownDevice() {
		assertThatThrownBy(() -> this.service.update(UUID.randomUUID(), "Porte", null, true))
			.isInstanceOf(DeviceNotFoundException.class);
	}

	@Test
	void updateShouldRejectDuplicateLabelOfAnotherDevice() {
		final Device device = this.deviceRepository.save(Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null));
		this.deviceRepository.save(Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Fenetre", null));

		assertThatThrownBy(() -> this.service.update(device.getId(), "Fenetre", null, true))
			.isInstanceOf(DuplicateDeviceLabelException.class);
	}

	@Test
	void updateShouldAllowKeepingTheSameLabel() {
		final Device device = this.deviceRepository.save(Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null));

		final Device updated = this.service.update(device.getId(), "Porte", null, true);

		assertThat(updated.getLabel()).isEqualTo("Porte");
	}

	@Test
	void updateShouldPublishDeviceUpdatedAndSupervisionChangedWithOldAndNewZone() {
		final UUID previousZoneId = UUID.randomUUID();
		final UUID newZoneId = UUID.randomUUID();
		final Device device = this.deviceRepository.save(
			Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", previousZoneId));

		this.service.update(device.getId(), "Porte", newZoneId, true);

		assertThat(this.publisher.updated).hasSize(1);
		assertThat(this.supervisionChangedPublisher.events).hasSize(1);
		final DeviceSupervisionChanged event = this.supervisionChangedPublisher.events.getFirst();
		assertThat(event.affectedZoneIds()).containsExactlyInAnyOrder(previousZoneId, newZoneId);
	}

	@Test
	void reactivatingADeviceShouldRefreshLastSeenAt() {
		final Device disabled = this.deviceRepository.save(
			Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null)
				.withEnabled(false)
				.withLastSeenAt(Instant.now().minus(Duration.ofDays(1))));

		final Device reactivated = this.service.update(disabled.getId(), "Porte", null, true);

		assertThat(reactivated.getLastSeenAt()).isAfter(Instant.now().minus(Duration.ofMinutes(1)));
	}

	private static final class InMemoryDeviceRepository implements DeviceRepository {

		private final Map<UUID, Device> devices = new HashMap<>();

		@Override
		public Device save(final Device device) {
			this.devices.put(device.getId(), device);
			return device;
		}

		@Override
		public Optional<Device> findById(final UUID id) {
			return Optional.ofNullable(this.devices.get(id));
		}

		@Override
		public List<Device> findAll() {
			return List.copyOf(this.devices.values());
		}

		@Override
		public List<Device> findByZoneId(final UUID zoneId) {
			return this.devices.values().stream().filter(device -> zoneId.equals(device.getZoneId())).toList();
		}

		@Override
		public Optional<Device> findByLabel(final String label) {
			return this.devices.values().stream().filter(device -> device.getLabel().equals(label)).findFirst();
		}

		@Override
		public void deleteById(final UUID id) {
			this.devices.remove(id);
		}
	}

	private static final class RecordingDevicePublisher implements DevicePublisher {

		private final List<DeviceCreated> created = new ArrayList<>();
		private final List<DeviceUpdated> updated = new ArrayList<>();
		private final List<DeviceDeleted> deleted = new ArrayList<>();

		@Override
		public void publish(final DeviceCreated event) {
			this.created.add(event);
		}

		@Override
		public void publish(final DeviceUpdated event) {
			this.updated.add(event);
		}

		@Override
		public void publish(final DeviceDeleted event) {
			this.deleted.add(event);
		}
	}

	private static final class RecordingSupervisionChangedPublisher implements DeviceSupervisionChangedPublisher {

		private final List<DeviceSupervisionChanged> events = new ArrayList<>();

		@Override
		public void publish(final DeviceSupervisionChanged event) {
			this.events.add(event);
		}
	}
}
