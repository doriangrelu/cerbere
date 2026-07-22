package fr.cerbere.component.cerbere_core.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine émis quand l'évaluation d'un {@code DeviceEventReport}
 * détecte une violation pendant que le système est armé — publié sur
 * {@code cerbere.alarm.alerts}. {@code cerbere-core} n'en conserve pas de copie
 * (voir ADR 0006) : l'historisation est à la charge de {@code cerbere-history}.
 */
public record AlertRaised(
	UUID alertId,
	UUID zoneId,
	UUID deviceId,
	AlertSeverity severity,
	String message,
	Instant occurredAt,
	UUID correlationId
) {
}
