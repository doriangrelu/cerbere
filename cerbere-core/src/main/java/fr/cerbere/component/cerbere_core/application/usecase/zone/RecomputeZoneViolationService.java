package fr.cerbere.component.cerbere_core.application.usecase.zone;

import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.Zone;
import fr.cerbere.component.cerbere_core.domain.port.in.zone.RecomputeZoneViolationUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Implémentation du use-case de recalcul de {@code Zone.violation}. Recalcule
 * toujours entièrement l'état à partir d'une requête live sur {@link DeviceRepository}
 * (jamais d'ajout/retrait incrémental) : seul endroit du code qui connaît à la
 * fois {@code Zone} et {@code Device} — voir ADR 0017.
 */
@RequiredArgsConstructor
public final class RecomputeZoneViolationService implements RecomputeZoneViolationUseCase {

    private final ZoneRepository zoneRepository;
    private final DeviceRepository deviceRepository;

    @Override
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
