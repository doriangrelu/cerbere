package fr.cerbere.component.cerbere_devices_mock.domain.event;

import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;

import java.util.UUID;

/**
 * Signal d'entrée représentant un événement de device rapporté depuis
 * l'extérieur (Kafka, via {@code cerbere.device.events.raw}). Volontairement
 * découplé de l'enveloppe Kafka partagée ({@code fr.cerbere.shared.event.EventEnvelope})
 * : le domaine ne connaît que ce dont il a besoin pour évaluer une alerte,
 * la traduction depuis l'enveloppe est un souci d'infrastructure.
 */
public record DeviceCreatedEvent(
        UUID id,
        DeviceType type,
        String label,
        UUID zoneId
) {
}
