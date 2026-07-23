package fr.cerbere.component.cerbere_core.application.usecase.zone;

import fr.cerbere.component.cerbere_core.domain.exception.ZoneNotEmptyException;
import fr.cerbere.component.cerbere_core.domain.exception.ZoneNotFoundException;
import fr.cerbere.component.cerbere_core.domain.port.in.zone.DeleteZoneUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Implémentation du use-case de suppression d'une zone. Une zone à laquelle
 * des devices sont encore rattachés ne peut pas être supprimée — vérifié par
 * requête live sur {@link DeviceRepository} (pas de collection de devices
 * maintenue sur {@code Zone}, voir ADR 0017).
 */
@RequiredArgsConstructor
public final class DeleteZoneService implements DeleteZoneUseCase {

	private final ZoneRepository zoneRepository;
	private final DeviceRepository deviceRepository;

	@Override
	public void delete(final UUID id) {
		this.zoneRepository.findById(id)
			.orElseThrow(() -> new ZoneNotFoundException(id));
		if (!this.deviceRepository.findByZoneId(id).isEmpty()) {
			throw new ZoneNotEmptyException(id);
		}
		this.zoneRepository.deleteById(id);
	}
}
