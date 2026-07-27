package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.exception.DeviceAlreadyBoundException;
import fr.cerbere.component.cerbere_devices_mock.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_mock.domain.model.ContactState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires purs (aucun contexte Spring) du use-case de liaison d'un
 * device simulé orphelin à un device du registre officiel.
 */
class BindSimulatedDeviceServiceTest {

	private InMemorySimulatedDeviceRepository repository;
	private BindSimulatedDeviceService service;

	@BeforeEach
	void setUp() {
		this.repository = new InMemorySimulatedDeviceRepository();
		this.service = new BindSimulatedDeviceService(this.repository);
	}

	@Test
	void bindShouldReKeyTheOrphanUnderTheCoreDeviceIdAndPreserveTypeAndState() {
		final UUID orphanId = UUID.randomUUID();
		final SimulatedDevice orphan = this.repository.save(SimulatedDevice.register(orphanId, DeviceType.CONTACT, "Capteur brut", null, false));
		this.repository.save(orphan.withState(ContactState.OPEN));
		final UUID coreDeviceId = UUID.randomUUID();
		final UUID zoneId = UUID.randomUUID();

		final SimulatedDevice bound = this.service.bind(orphanId, coreDeviceId, "PORTE D'ENTREE", zoneId);

		assertThat(bound.getId()).isEqualTo(coreDeviceId);
		assertThat(bound.getLabel()).isEqualTo("PORTE D'ENTREE");
		assertThat(bound.getZoneId()).isEqualTo(zoneId);
		assertThat(bound.getType()).isEqualTo(DeviceType.CONTACT);
		assertThat(bound.getCurrentState()).isEqualTo(ContactState.OPEN);
		assertThat(this.repository.findById(orphanId)).isEmpty();
		assertThat(this.repository.findById(coreDeviceId)).contains(bound);
	}

	@Test
	void bindShouldThrowWhenOrphanDoesNotExist() {
		assertThatThrownBy(() -> this.service.bind(UUID.randomUUID(), UUID.randomUUID(), "LABEL", null))
			.isInstanceOf(DeviceNotFoundException.class);
	}

	@Test
	void bindShouldThrowWhenCoreDeviceIdAlreadyBound() {
		final UUID orphanId = UUID.randomUUID();
		this.repository.save(SimulatedDevice.register(orphanId, DeviceType.MOTION, "Capteur brut", null, false));
		final UUID coreDeviceId = UUID.randomUUID();
		this.repository.save(SimulatedDevice.register(coreDeviceId, DeviceType.MOTION, "Deja lie", null, false));

		assertThatThrownBy(() -> this.service.bind(orphanId, coreDeviceId, "LABEL", null))
			.isInstanceOf(DeviceAlreadyBoundException.class);
	}

	private static final class InMemorySimulatedDeviceRepository implements SimulatedDeviceRepository {

		private final Map<UUID, SimulatedDevice> devices = new HashMap<>();

		@Override
		public SimulatedDevice save(final SimulatedDevice device) {
			this.devices.put(device.getId(), device);
			return device;
		}

		@Override
		public Optional<SimulatedDevice> findById(final UUID id) {
			return Optional.ofNullable(this.devices.get(id));
		}

		@Override
		public List<SimulatedDevice> findAll() {
			return List.copyOf(this.devices.values());
		}

		@Override
		public List<SimulatedDevice> findByAutoSimulateTrue() {
			return this.devices.values().stream().filter(SimulatedDevice::isAutoSimulate).toList();
		}

		@Override
		public void deleteById(final UUID id) {
			this.devices.remove(id);
		}
	}
}
