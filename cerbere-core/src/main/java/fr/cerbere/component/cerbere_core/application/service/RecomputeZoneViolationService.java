package fr.cerbere.component.cerbere_core.application.service;

import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.Zone;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Recalcule entièrement (jamais de façon incrémentale) l'état de violation
 * d'une zone à partir des devices qui lui sont actuellement rattachés, pour
 * éviter tout mirroir qui dérive de la réalité — voir ADR 0017. Collaborateur
 * interne à la couche application : n'implémente aucun port d'entrée, aucun
 * adapter ne l'appelle directement — voir ADR 0018.
 */
@RequiredArgsConstructor
public final class RecomputeZoneViolationService {

	private final ZoneRepository zoneRepository;
	private final DeviceRepository deviceRepository;

	public void recompute(final UUID zoneId) {
		if (zoneId == null) {
			return;
		}
		this.zoneRepository.findById(zoneId).ifPresent(this::recomputeFor);
	}

	private void recomputeFor(final Zone zone) {
		final boolean violation = this.deviceRepository.findByZoneId(zone.getId()).stream()
			.filter(Device::isEnabled)
			.anyMatch(Device::isViolation);
		if (violation != zone.isViolation()) {
			this.zoneRepository.save(violation ? zone.withViolation() : zone.withoutViolation());
		}
	}
}
