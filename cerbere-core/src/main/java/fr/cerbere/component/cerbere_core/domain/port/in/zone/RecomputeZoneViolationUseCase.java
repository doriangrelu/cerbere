package fr.cerbere.component.cerbere_core.domain.port.in.zone;

import java.util.UUID;

/**
 * Recalcule l'état {@code violation} d'une zone à partir des devices qui lui
 * sont actuellement rattachés — jamais de mise à jour incrémentale, voir ADR 0017.
 */
public interface RecomputeZoneViolationUseCase {

	void recompute(UUID zoneId);
}
