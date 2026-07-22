package fr.cerbere.component.cerbere_core.domain.event;

import fr.cerbere.component.cerbere_core.domain.model.DeviceType;

import java.time.Instant;
import java.util.UUID;

public record DeviceCreated(
        UUID id,
        String label,
        DeviceType type,
        UUID zoneId,
        Instant occurredAt,
        UUID correlationId
) {
}
