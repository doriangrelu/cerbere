package fr.cerbere.shared.event;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Enveloppe d'événement commune à tout Cerbère (voir ADR 0002 et
 * docs/best-practices/kafka-conventions.md). Portée par le module partagé
 * {@code cerbere-shared-kernel} (voir ADR 0010, ADR 0013) : tout service qui
 * publie ou consomme un événement Kafka Cerbère dépend de ce même type,
 * plutôt que de redéfinir sa propre copie.
 */
public record EventEnvelope(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Instant producedAt,
        String source,
        UUID deviceId,
        UUID zoneId,
        UUID correlationId,
        Map<String, Object> payload
) {

    public <T> Optional<T> getPayloadValue(final String key, final Class<T> targetType) {
        return Optional.ofNullable(this.payload.get(key))
                .filter(targetType::isInstance)
                .map(targetType::cast);
    }

    public Optional<String> getPayloadValue(final String key) {
        return this.getPayloadValue(key, String.class);
    }

    public <T> Optional<T> getPayloadValue(final String key, final Function<Object, T> converter) {
        return Optional.ofNullable(this.payload.get(key))
                .map(converter);
    }

}
