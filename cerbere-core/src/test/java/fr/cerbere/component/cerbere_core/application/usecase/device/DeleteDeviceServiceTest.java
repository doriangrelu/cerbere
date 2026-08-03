package fr.cerbere.component.cerbere_core.application.usecase.device;

import fr.cerbere.component.cerbere_core.domain.event.DeviceCreated;
import fr.cerbere.component.cerbere_core.domain.event.DeviceDeleted;
import fr.cerbere.component.cerbere_core.domain.event.DeviceSupervisionChanged;
import fr.cerbere.component.cerbere_core.domain.event.DeviceUpdated;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.DeviceType;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DevicePublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceSupervisionChangedPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeleteDeviceServiceTest {

	private InMemoryDeviceRepository deviceRepository;
	private RecordingDevicePublisher publisher;
	private RecordingSupervisionChangedPublisher supervisionChangedPublisher;
	private DeleteDeviceService service;

	@BeforeEach
	void setUp() {
		this.deviceRepository = new InMemoryDeviceRepository();
		this.publisher = new RecordingDevicePublisher();
		this.supervisionChangedPublisher = new RecordingSupervisionChangedPublisher();
		this.service = new DeleteDeviceService(this.deviceRepository, this.publisher, this.supervisionChangedPublisher);
	}

	@Test
	void deleteShouldRemoveDeviceAndPublishDeviceDeletedAndSupervisionChanged() {
		final UUID zoneId = UUID.randomUUID();
		final Device device = this.deviceRepository.save(Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", zoneId));

		this.service.delete(device.getId());

		assertThat(this.deviceRepository.findById(device.getId())).isEmpty();
		assertThat(this.publisher.deleted).hasSize(1);
		assertThat(this.publisher.deleted.getFirst().id()).isEqualTo(device.getId());
		assertThat(this.supervisionChangedPublisher.events).hasSize(1);
		assertThat(this.supervisionChangedPublisher.events.getFirst().affectedZoneIds()).containsExactly(zoneId);
	}

	@Test
	void deleteOfAnUnknownDeviceShouldStillPublishWithoutAZone() {
		final UUID id = UUID.randomUUID();

		this.service.delete(id);

		assertThat(this.publisher.deleted).hasSize(1);
		assertThat(this.supervisionChangedPublisher.events.getFirst().affectedZoneIds()).isEmpty();
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
