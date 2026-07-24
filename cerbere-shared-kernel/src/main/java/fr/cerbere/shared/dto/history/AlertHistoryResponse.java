package fr.cerbere.shared.dto.history;

import java.time.Instant;

/**
 * Contrat REST partagé entre {@code cerbere-history} (producteur) et
 * {@code cerbere-bff} (consommateur) — voir ADR 0010/0013. Reflète une
 * alerte levée historisée depuis {@code cerbere.alarm.alerts} — voir ADR 0006
 * (pas de copie locale des alertes côté {@code cerbere-core}).
 */
public record AlertHistoryResponse(
	String eventId,
	String deviceId,
	String zoneId,
	String severity,
	String message,
	Instant occurredAt,
	Instant recordedAt,
	String correlationId
) {
}
