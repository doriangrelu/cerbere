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

class RecordDiscoveredDeviceServiceTest {

	private InMemoryDiscoveredDeviceRepository repository;
	private RecordDiscoveredDeviceService service;

	@BeforeEach
	void setUp() {
		this.repository = new InMemoryDiscoveredDeviceRepository();
		this.service = new RecordDiscoveredDeviceService(this.repository);
	}

	@Test
	void recordShouldDiscoverANewDevice() {
		this.service.record("0x00158d0001", DeviceType.CONTACT);

		final DiscoveredDevice discovered = this.repository.findByFriendlyName("0x00158d0001").orElseThrow();
		assertThat(discovered.getInferredType()).isEqualTo(DeviceType.CONTACT);
	}

	@Test
	void recordShouldRefreshLastSeenAtOnARepeatedObservation() {
		this.service.record("0x00158d0001", DeviceType.CONTACT);
		final Instant firstSeenAt = this.repository.findByFriendlyName("0x00158d0001").orElseThrow().getLastSeenAt();

		this.service.record("0x00158d0001", DeviceType.CONTACT);

		final Instant secondSeenAt = this.repository.findByFriendlyName("0x00158d0001").orElseThrow().getLastSeenAt();
		assertThat(secondSeenAt).isAfterOrEqualTo(firstSeenAt);
	}

	@Test
	void recordShouldNotOverwriteAnAlreadyKnownTypeWithAnUndeterminedOne() {
		this.service.record("0x00158d0001", DeviceType.CONTACT);

		this.service.record("0x00158d0001", null);

		assertThat(this.repository.findByFriendlyName("0x00158d0001").orElseThrow().getInferredType())
			.isEqualTo(DeviceType.CONTACT);
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
