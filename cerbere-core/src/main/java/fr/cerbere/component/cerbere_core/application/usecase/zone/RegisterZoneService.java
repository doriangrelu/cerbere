package fr.cerbere.component.cerbere_core.application.usecase.zone;

import fr.cerbere.component.cerbere_core.domain.model.Zone;
import fr.cerbere.component.cerbere_core.domain.port.in.zone.RegisterZoneUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;

/**
 * Implémentation du use-case d'enregistrement d'une zone.
 */
@RequiredArgsConstructor
public final class RegisterZoneService implements RegisterZoneUseCase {

	private final ZoneRepository zoneRepository;

	@Override
	public Zone register(final String name) {
		final Zone zone = Zone.register(name);
		return this.zoneRepository.save(zone);
	}
}
