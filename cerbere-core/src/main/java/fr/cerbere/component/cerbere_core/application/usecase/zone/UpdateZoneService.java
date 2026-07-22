package fr.cerbere.component.cerbere_core.application.usecase.zone;

import fr.cerbere.component.cerbere_core.domain.exception.ZoneNotFoundException;
import fr.cerbere.component.cerbere_core.domain.model.Zone;
import fr.cerbere.component.cerbere_core.domain.port.in.zone.UpdateZoneUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Implémentation du use-case de renommage d'une zone.
 */
@RequiredArgsConstructor
public final class UpdateZoneService implements UpdateZoneUseCase {

	private final ZoneRepository zoneRepository;

	@Override
	public Zone update(final UUID id, final String name) {
		final Zone zone = this.zoneRepository.findById(id)
			.orElseThrow(() -> new ZoneNotFoundException(id));
		return this.zoneRepository.save(zone.withName(name));
	}
}
