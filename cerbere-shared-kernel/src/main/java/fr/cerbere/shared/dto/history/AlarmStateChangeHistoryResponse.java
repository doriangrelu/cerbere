package fr.cerbere.shared.dto.history;

import java.time.Instant;

/**
 * Contrat REST partagé entre {@code cerbere-history} (producteur) et
 * {@code cerbere-bff} (consommateur) — voir ADR 0010/0013. Reflète un
 * changement d'état de l'alarme (armement/désarmement/déclenchement)
 * historisé depuis {@code cerbere.alarm.state-changed}.
 */
public record AlarmStateChangeHistoryResponse(
	String eventId,
	String previousMode,
	String newMode,
	boolean triggered,
	Instant occurredAt,
	Instant recordedAt,
	String correlationId
) {
}
