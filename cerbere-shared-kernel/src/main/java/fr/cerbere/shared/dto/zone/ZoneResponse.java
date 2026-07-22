package fr.cerbere.shared.dto.zone;

import java.util.List;

/**
 * Contrat REST partagé entre {@code cerbere-core} (producteur) et
 * {@code cerbere-bff} (consommateur) — voir ADR 0010/0013.
 */
public record ZoneResponse(
	String id,
	String name,
	List<String> deviceIds
) {
}
