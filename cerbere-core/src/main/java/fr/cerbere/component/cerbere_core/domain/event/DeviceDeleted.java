package fr.cerbere.component.cerbere_core.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine émis quand un device est supprimé du registre officiel.
 * Permet à {@code cerbere-devices-mock} de retirer son miroir correspondant —
 * voir ADR 0016.
 */
public record DeviceDeleted(
        UUID id,
        Instant occurredAt,
        UUID correlationId
) {
}
