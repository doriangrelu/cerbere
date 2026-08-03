package fr.cerbere.component.cerbere_devices_bridge.application.usecase;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DiscoveredDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DiscoveredDeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ListDiscoveredDevicesServiceTest {

	private InMemoryDiscoveredDeviceRepository repository;
	private ListDiscoveredDevicesService service;

	@BeforeEach
	void setUp() {
		this.repository = new InMemoryDiscoveredDeviceRepository();
		this.service = new ListDiscoveredDevicesService(this.repository);
	}

	@Test
	void listAllShouldReturnEveryDiscoveredDevice() {
		final DiscoveredDevice device = this.repository.save(
			DiscoveredDevice.discover("0x00158d0001", DeviceType.CONTACT, Instant.now()));

		assertThat(this.service.listAll()).containsExactly(device);
	}

	@Test
	void listAllShouldReturnAnEmptyListWhenNothingHasBeenDiscovered() {
		assertThat(this.service.listAll()).isEmpty();
	}

	private static final class InMemoryDiscoveredDeviceRepository implements DiscoveredDeviceRepository {

		private final Map<String, DiscoveredDevice> devices = new HashMap<>();

		@Override
		public DiscoveredDevice save(final DiscoveredDevice device) {
			this.devices.put(device.getFriendlyName(), device);
			return device;
		}

		@Override
		public Optional<DiscoveredDevice> findByFriendlyName(final String friendlyName) {
			return Optional.ofNullable(this.devices.get(friendlyName));
		}

		@Override
		public List<DiscoveredDevice> findAll() {
			return List.copyOf(this.devices.values());
		}

		@Override
		public void deleteByFriendlyName(final String friendlyName) {
			this.devices.remove(friendlyName);
		}
	}
}
