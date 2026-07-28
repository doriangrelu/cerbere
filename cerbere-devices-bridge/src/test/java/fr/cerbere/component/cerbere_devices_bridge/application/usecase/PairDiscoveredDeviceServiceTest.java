package fr.cerbere.component.cerbere_devices_bridge.application.usecase;

import fr.cerbere.component.cerbere_devices_bridge.domain.exception.DiscoveredDeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DiscoveredDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DeviceRenamePublisher;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DiscoveredDeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires purs (aucun contexte Spring) de l'appairage d'un device
 * découvert (voir ADR 0023).
 */
class PairDiscoveredDeviceServiceTest {

	private InMemoryDiscoveredDeviceRepository repository;
	private RecordingDeviceRenamePublisher renamePublisher;
	private PairDiscoveredDeviceService service;

	@BeforeEach
	void setUp() {
		this.repository = new InMemoryDiscoveredDeviceRepository();
		this.renamePublisher = new RecordingDeviceRenamePublisher();
		this.service = new PairDiscoveredDeviceService(this.repository, this.renamePublisher);
	}

	@Test
	void pairShouldRequestRenameToCoreDeviceIdAndDropTheCandidate() {
		this.repository.save(DiscoveredDevice.discover("0x00124b0022qs", DeviceType.CONTACT, Instant.now()));
		final UUID coreDeviceId = UUID.randomUUID();

		this.service.pair("0x00124b0022qs", coreDeviceId);

		assertThat(this.renamePublisher.renames()).containsExactly(
			new Rename("0x00124b0022qs", coreDeviceId.toString())
		);
		assertThat(this.repository.findByFriendlyName("0x00124b0022qs")).isEmpty();
	}

	@Test
	void pairShouldThrowWhenFriendlyNameWasNeverSeen() {
		assertThatThrownBy(() -> this.service.pair("jamais-vu", UUID.randomUUID()))
			.isInstanceOf(DiscoveredDeviceNotFoundException.class);
	}

	@Test
	void pairShouldNotRenameWhenFriendlyNameWasNeverSeen() {
		assertThatThrownBy(() -> this.service.pair("jamais-vu", UUID.randomUUID()))
			.isInstanceOf(DiscoveredDeviceNotFoundException.class);

		assertThat(this.renamePublisher.renames()).isEmpty();
	}

	private record Rename(String from, String to) {
	}

	private static final class InMemoryDiscoveredDeviceRepository implements DiscoveredDeviceRepository {

		private final Map<String, DiscoveredDevice> devices = new LinkedHashMap<>();

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

	private static final class RecordingDeviceRenamePublisher implements DeviceRenamePublisher {

		private final List<Rename> renames = new ArrayList<>();

		@Override
		public void rename(final String currentFriendlyName, final String newFriendlyName) {
			this.renames.add(new Rename(currentFriendlyName, newFriendlyName));
		}

		List<Rename> renames() {
			return this.renames;
		}
	}
}
