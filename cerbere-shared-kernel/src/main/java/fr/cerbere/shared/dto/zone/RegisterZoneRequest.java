package fr.cerbere.shared.dto.zone;

import jakarta.validation.constraints.NotBlank;

/**
 * Contrat REST partagé entre {@code cerbere-bff} (émetteur) et
 * {@code cerbere-core} (récepteur, {@code POST /api/zones}).
 */
public record RegisterZoneRequest(@NotBlank String name) {
}
