package fr.cerbere.component.cerbere_core.application.usecase.zone;

import fr.cerbere.component.cerbere_core.domain.model.Zone;
import fr.cerbere.component.cerbere_core.domain.port.in.zone.ListZonesUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Implémentation du use-case de consultation des zones.
 */
@RequiredArgsConstructor
public final class ListZonesService implements ListZonesUseCase {

	private final ZoneRepository zoneRepository;

	@Override
	public List<Zone> listAll() {
		return this.zoneRepository.findAll();
	}
}
