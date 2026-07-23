package fr.cerbere.component.cerbere_core.infrastructure.messaging.event;

import fr.cerbere.component.cerbere_core.domain.event.DeviceCreated;
import fr.cerbere.component.cerbere_core.domain.event.DeviceDeleted;
import fr.cerbere.component.cerbere_core.domain.event.DeviceUpdated;
import fr.cerbere.shared.event.EventEnvelope;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@UtilityClass
public final class DeviceEventFactory {

    private static final String SOURCE = "cerbere-core";

    public static EventEnvelope from(final DeviceCreated deviceCreated) {
        return new EventEnvelope(
                UUID.randomUUID(),
                "device.created",
                deviceCreated.occurredAt(),
                Instant.now(),
                SOURCE,
                deviceCreated.id(),
                deviceCreated.zoneId(),
                deviceCreated.correlationId(),
                Map.of(
                        "type", deviceCreated.type(),
                        "label", deviceCreated.label()
                )
        );
    }

    public static EventEnvelope from(final DeviceUpdated deviceUpdated) {
        return new EventEnvelope(
                UUID.randomUUID(),
                "device.updated",
                deviceUpdated.occurredAt(),
                Instant.now(),
                SOURCE,
                deviceUpdated.id(),
                deviceUpdated.zoneId(),
                deviceUpdated.correlationId(),
                Map.of("label", deviceUpdated.label())
        );
    }

    public static EventEnvelope from(final DeviceDeleted deviceDeleted) {
        return new EventEnvelope(
                UUID.randomUUID(),
                "device.deleted",
                deviceDeleted.occurredAt(),
                Instant.now(),
                SOURCE,
                deviceDeleted.id(),
                null,
                deviceDeleted.correlationId(),
                Map.of()
        );
    }

}
