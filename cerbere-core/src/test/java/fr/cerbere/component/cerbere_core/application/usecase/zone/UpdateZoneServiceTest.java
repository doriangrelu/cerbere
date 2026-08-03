package fr.cerbere.component.cerbere_core.application.usecase.zone;

import fr.cerbere.component.cerbere_core.domain.exception.DuplicateZoneNameException;
import fr.cerbere.component.cerbere_core.domain.exception.ZoneNotFoundException;
import fr.cerbere.component.cerbere_core.domain.model.Zone;
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

class UpdateZoneServiceTest {

	private InMemoryZoneRepository zoneRepository;
	private UpdateZoneService service;

	@BeforeEach
	void setUp() {
		this.zoneRepository = new InMemoryZoneRepository();
		this.service = new UpdateZoneService(this.zoneRepository);
	}

	@Test
	void updateShouldRejectUnknownZone() {
		assertThatThrownBy(() -> this.service.update(UUID.randomUUID(), "Garage"))
			.isInstanceOf(ZoneNotFoundException.class);
	}

	@Test
	void updateShouldRejectDuplicateNameOfAnotherZone() {
		final Zone zone = this.zoneRepository.save(Zone.register("Étage"));
		this.zoneRepository.save(Zone.register("Garage"));

		assertThatThrownBy(() -> this.service.update(zone.getId(), "Garage"))
			.isInstanceOf(DuplicateZoneNameException.class);
	}

	@Test
	void updateShouldAllowKeepingTheSameName() {
		final Zone zone = this.zoneRepository.save(Zone.register("Étage"));

		final Zone updated = this.service.update(zone.getId(), "Étage");

		assertThat(updated.getName()).isEqualTo("Étage");
	}

	@Test
	void updateShouldRenameTheZone() {
		final Zone zone = this.zoneRepository.save(Zone.register("Étage"));

		final Zone renamed = this.service.update(zone.getId(), "Combles");

		assertThat(renamed.getName()).isEqualTo("Combles");
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
}
