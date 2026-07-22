package fr.cerbere.shared.dto.alarm;

/**
 * Contrat REST partagé entre {@code cerbere-core} (producteur, endpoint
 * {@code GET/POST /api/alarm/*}) et {@code cerbere-bff} (consommateur) — voir
 * ADR 0010/0013 : tout DTO consommé par plus d'un module vit dans
 * {@code cerbere-shared-kernel} plutôt que d'être dupliqué.
 */
public record AlarmStatusResponse(
	String systemId,
	String mode,
	boolean triggered,
	String status
) {
}
