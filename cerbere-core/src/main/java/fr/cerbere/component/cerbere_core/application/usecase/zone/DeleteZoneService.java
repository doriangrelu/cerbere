package fr.cerbere.component.cerbere_core.application.usecase.zone;

import fr.cerbere.component.cerbere_core.domain.exception.ZoneNotEmptyException;
import fr.cerbere.component.cerbere_core.domain.exception.ZoneNotFoundException;
import fr.cerbere.component.cerbere_core.domain.model.Zone;
import fr.cerbere.component.cerbere_core.domain.port.in.zone.DeleteZoneUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Implémentation du use-case de suppression d'une zone. Une zone à laquelle
 * des devices sont encore rattachés ne peut pas être supprimée.
 */
@RequiredArgsConstructor
public final class DeleteZoneService implements DeleteZoneUseCase {

	private final ZoneRepository zoneRepository;

	@Override
	public void delete(final UUID id) {
		final Zone zone = this.zoneRepository.findById(id)
			.orElseThrow(() -> new ZoneNotFoundException(id));
		if (!zone.getDeviceIds().isEmpty()) {
			throw new ZoneNotEmptyException(id);
		}
		this.zoneRepository.deleteById(id);
	}
}
