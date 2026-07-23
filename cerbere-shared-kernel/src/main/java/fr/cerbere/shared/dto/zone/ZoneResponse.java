package fr.cerbere.shared.dto.zone;

/**
 * Contrat REST partagé entre {@code cerbere-core} (producteur) et
 * {@code cerbere-bff} (consommateur) — voir ADR 0010/0013. Ne porte pas la
 * liste des devices rattachés (agrégat séparé, voir ADR 0017) : {@code cerbere-bff}
 * la dérive lui-même de {@code DeviceResponse.zoneId()}.
 */
public record ZoneResponse(
	String id,
	String name,
	boolean violation
) {
}
