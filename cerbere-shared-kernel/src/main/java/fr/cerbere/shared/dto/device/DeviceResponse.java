package fr.cerbere.shared.dto.device;

/**
 * Contrat REST partagé entre {@code cerbere-core} (producteur) et
 * {@code cerbere-bff} (consommateur) — voir ADR 0010/0013. {@code violation}
 * reflète l'état dérivé exposé par {@code Device.isViolation()} (toujours
 * {@code false} si le device est désactivé), pas le drapeau brut persisté.
 */
public record DeviceResponse(
	String id,
	String type,
	String label,
	String zoneId,
	boolean enabled,
	boolean violation
) {
}
