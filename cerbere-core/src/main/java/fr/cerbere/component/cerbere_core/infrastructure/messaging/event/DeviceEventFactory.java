package fr.cerbere.component.cerbere_core.infrastructure.messaging.event;

import fr.cerbere.component.cerbere_core.domain.event.DeviceCreated;
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


}
