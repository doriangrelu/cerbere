package fr.cerbere.component.cerbere_devices_bridge.application.usecase;

import fr.cerbere.component.cerbere_devices_bridge.domain.exception.DeviceTypeMismatchException;
import fr.cerbere.component.cerbere_devices_bridge.domain.exception.DiscoveredDeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.BridgedDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DiscoveredDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.BridgedDeviceRepository;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DeviceRenamePublisher;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DiscoveredDeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
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

	private static final String FRIENDLY_NAME = "0x00124b0022qs";

	private InMemoryDiscoveredDeviceRepository discoveredDeviceRepository;
	private InMemoryBridgedDeviceRepository bridgedDeviceRepository;
	private RecordingDeviceRenamePublisher renamePublisher;
	private PairDiscoveredDeviceService service;

	@BeforeEach
	void setUp() {
		this.discoveredDeviceRepository = new InMemoryDiscoveredDeviceRepository();
		this.bridgedDeviceRepository = new InMemoryBridgedDeviceRepository();
		this.renamePublisher = new RecordingDeviceRenamePublisher();
		this.service = new PairDiscoveredDeviceService(
			this.discoveredDeviceRepository, this.bridgedDeviceRepository, this.renamePublisher
		);
	}

	@Test
	void pairShouldRequestRenameToCoreDeviceIdAndDropTheCandidate() {
		this.discoveredDeviceRepository.save(DiscoveredDevice.discover(FRIENDLY_NAME, DeviceType.CONTACT, Instant.now()));
		final UUID coreDeviceId = this.givenBridgedDevice(DeviceType.CONTACT);

		this.service.pair(FRIENDLY_NAME, coreDeviceId);

		assertThat(this.renamePublisher.renames()).containsExactly(new Rename(FRIENDLY_NAME, coreDeviceId.toString()));
		assertThat(this.discoveredDeviceRepository.findByFriendlyName(FRIENDLY_NAME)).isEmpty();
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

	@Test
	void pairShouldRejectAMotionSensorOnAContactDevice() {
		this.discoveredDeviceRepository.save(DiscoveredDevice.discover(FRIENDLY_NAME, DeviceType.MOTION, Instant.now()));
		final UUID coreDeviceId = this.givenBridgedDevice(DeviceType.CONTACT);

		assertThatThrownBy(() -> this.service.pair(FRIENDLY_NAME, coreDeviceId))
			.isInstanceOf(DeviceTypeMismatchException.class);
	}

	@Test
	void pairShouldLeaveTheCandidateUntouchedWhenTypesDoNotMatch() {
		this.discoveredDeviceRepository.save(DiscoveredDevice.discover(FRIENDLY_NAME, DeviceType.MOTION, Instant.now()));
		final UUID coreDeviceId = this.givenBridgedDevice(DeviceType.SIREN);

		assertThatThrownBy(() -> this.service.pair(FRIENDLY_NAME, coreDeviceId))
			.isInstanceOf(DeviceTypeMismatchException.class);

		assertThat(this.renamePublisher.renames()).isEmpty();
		assertThat(this.discoveredDeviceRepository.findByFriendlyName(FRIENDLY_NAME)).isPresent();
	}

	@Test
	void pairShouldAllowAnUndeterminedTypeSinceThereIsNothingToCompare() {
		this.discoveredDeviceRepository.save(DiscoveredDevice.discover(FRIENDLY_NAME, null, Instant.now()));
		final UUID coreDeviceId = this.givenBridgedDevice(DeviceType.CONTACT);

		this.service.pair(FRIENDLY_NAME, coreDeviceId);

		assertThat(this.renamePublisher.renames()).hasSize(1);
	}

	@Test
	void pairShouldAllowWhenTheCoreDeviceMirrorHasNotPropagatedYet() {
		this.discoveredDeviceRepository.save(DiscoveredDevice.discover(FRIENDLY_NAME, DeviceType.MOTION, Instant.now()));

		this.service.pair(FRIENDLY_NAME, UUID.randomUUID());

		assertThat(this.renamePublisher.renames()).hasSize(1);
	}

	private UUID givenBridgedDevice(final DeviceType type) {
		final UUID id = UUID.randomUUID();
		this.bridgedDeviceRepository.save(BridgedDevice.register(id, type, "Device officiel", null));
		return id;
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
