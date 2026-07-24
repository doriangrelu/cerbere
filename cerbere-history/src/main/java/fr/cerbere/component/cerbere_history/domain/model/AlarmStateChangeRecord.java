package fr.cerbere.component.cerbere_history.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Historisation d'un changement d'état de l'alarme (armement/désarmement/
 * déclenchement), reçu via {@code cerbere.alarm.state-changed}. {@code eventId}
 * sert d'identifiant de déduplication, voir {@link DeviceEventRecord}.
 */
public record AlarmStateChangeRecord(
	UUID eventId,
	AlarmMode previousMode,
	AlarmMode newMode,
	boolean triggered,
	Instant occurredAt,
	Instant recordedAt,
	UUID correlationId
) {
}
