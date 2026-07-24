package fr.cerbere.component.cerbere_history.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Historisation d'une alerte levée, reçue via {@code cerbere.alarm.alerts}.
 * {@code cerbere-core} ne conserve aucune copie locale des alertes (voir ADR 0006) :
 * cet enregistrement en est la source de vérité durable. {@code eventId} sert
 * d'identifiant de déduplication, voir {@link DeviceEventRecord}.
 */
public record AlertRecord(
	UUID eventId,
	UUID deviceId,
	UUID zoneId,
	AlertSeverity severity,
	String message,
	Instant occurredAt,
	Instant recordedAt,
	UUID correlationId
) {
}
