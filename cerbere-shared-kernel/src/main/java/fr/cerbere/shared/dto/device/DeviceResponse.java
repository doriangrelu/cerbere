package fr.cerbere.shared.dto.device;

import java.time.Instant;

/**
 * Contrat REST partagé entre {@code cerbere-core} (producteur) et
 * {@code cerbere-bff} (consommateur) — voir ADR 0010/0013. {@code violation}
 * reflète l'état dérivé exposé par {@code Device.isViolation()} (toujours
 * {@code false} si le device est désactivé), pas le drapeau brut persisté.
 * {@code lastSeenAt} : dernier événement reçu pour ce device (ou date de
 * création si aucun événement reçu depuis) — voir ADR 0020 (supervision de vie).
 */
public record DeviceResponse(
	String id,
	String type,
	String label,
	String zoneId,
	boolean enabled,
	boolean violation,
	Instant lastSeenAt
) {
}
