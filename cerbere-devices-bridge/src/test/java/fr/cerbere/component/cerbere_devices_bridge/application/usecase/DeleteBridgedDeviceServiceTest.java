package fr.cerbere.component.cerbere_devices_bridge.application.usecase;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.BridgedDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.BridgedDeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class DeleteBridgedDeviceServiceTest {

	private InMemoryBridgedDeviceRepository repository;
	private DeleteBridgedDeviceService service;

	@BeforeEach
	void setUp() {
		this.repository = new InMemoryBridgedDeviceRepository();
		this.service = new DeleteBridgedDeviceService(this.repository);
	}

	@Test
	void deleteShouldRemoveTheMirror() {
		final BridgedDevice device = this.repository.save(
			BridgedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null));

		this.service.delete(device.getId());

		assertThat(this.repository.findById(device.getId())).isEmpty();
	}

	@Test
	void deleteShouldBeIdempotentOnAnUnknownMirror() {
		assertThatCode(() -> this.service.delete(UUID.randomUUID())).doesNotThrowAnyException();
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
}
