package fr.cerbere.component.cerbere_core.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine émis quand un device est modifié (libellé, zone). Le
 * type n'est pas transporté : il n'est jamais modifiable après création (voir
 * {@link fr.cerbere.component.cerbere_core.domain.model.Device}). Permet à
 * {@code cerbere-devices-mock} de garder son miroir aligné — voir ADR 0016.
 */
public record DeviceUpdated(
        UUID id,
        String label,
        UUID zoneId,
        Instant occurredAt,
        UUID correlationId
) {
}
