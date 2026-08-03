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

class RegisterBridgedDeviceServiceTest {

	private InMemoryBridgedDeviceRepository repository;
	private RegisterBridgedDeviceService service;

	@BeforeEach
	void setUp() {
		this.repository = new InMemoryBridgedDeviceRepository();
		this.service = new RegisterBridgedDeviceService(this.repository);
	}

	@Test
	void registerShouldSaveTheMirror() {
		final UUID id = UUID.randomUUID();
		final UUID zoneId = UUID.randomUUID();

		final BridgedDevice saved = this.service.register(id, DeviceType.CONTACT, "Porte d'entrée", zoneId);

		assertThat(this.repository.findById(id)).contains(saved);
		assertThat(saved.getType()).isEqualTo(DeviceType.CONTACT);
		assertThat(saved.getLabel()).isEqualTo("Porte d'entrée");
		assertThat(saved.getZoneId()).isEqualTo(zoneId);
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
