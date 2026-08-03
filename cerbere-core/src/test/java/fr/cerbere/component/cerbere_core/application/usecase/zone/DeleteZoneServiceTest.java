package fr.cerbere.component.cerbere_core.application.usecase.zone;

import fr.cerbere.component.cerbere_core.domain.exception.ZoneNotEmptyException;
import fr.cerbere.component.cerbere_core.domain.exception.ZoneNotFoundException;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.DeviceType;
import fr.cerbere.component.cerbere_core.domain.model.Zone;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.zone.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeleteZoneServiceTest {

	private InMemoryZoneRepository zoneRepository;
	private InMemoryDeviceRepository deviceRepository;
	private DeleteZoneService service;

	@BeforeEach
	void setUp() {
		this.zoneRepository = new InMemoryZoneRepository();
		this.deviceRepository = new InMemoryDeviceRepository();
		this.service = new DeleteZoneService(this.zoneRepository, this.deviceRepository);
	}

	@Test
	void deleteShouldRejectUnknownZone() {
		assertThatThrownBy(() -> this.service.delete(UUID.randomUUID()))
			.isInstanceOf(ZoneNotFoundException.class);
	}

	@Test
	void deleteShouldRejectANonEmptyZone() {
		final Zone zone = this.zoneRepository.save(Zone.register("Étage"));
		this.deviceRepository.save(Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", zone.getId()));

		assertThatThrownBy(() -> this.service.delete(zone.getId()))
			.isInstanceOf(ZoneNotEmptyException.class);
	}

	@Test
	void deleteShouldRemoveAnEmptyZone() {
		final Zone zone = this.zoneRepository.save(Zone.register("Étage"));

		this.service.delete(zone.getId());

		assertThat(this.zoneRepository.findById(zone.getId())).isEmpty();
	}

	private static final class InMemoryZoneRepository implements ZoneRepository {

		private final Map<UUID, Zone> zones = new HashMap<>();

		@Override
		public Zone save(final Zone zone) {
			this.zones.put(zone.getId(), zone);
			return zone;
		}

		@Override
		public Optional<Zone> findById(final UUID id) {
			return Optional.ofNullable(this.zones.get(id));
		}

		@Override
		public List<Zone> findAll() {
			return List.copyOf(this.zones.values());
		}

		@Override
		public Optional<Zone> findByName(final String name) {
			return this.zones.values().stream().filter(zone -> zone.getName().equals(name)).findFirst();
		}

		@Override
		public void deleteById(final UUID id) {
			this.zones.remove(id);
		}
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
}
