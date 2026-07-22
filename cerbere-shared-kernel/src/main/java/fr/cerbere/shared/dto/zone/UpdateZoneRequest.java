package fr.cerbere.shared.dto.zone;

import jakarta.validation.constraints.NotBlank;

/**
 * Contrat REST partagé entre {@code cerbere-bff} (émetteur) et
 * {@code cerbere-core} (récepteur, {@code PUT /api/zones/{id}}).
 */
public record UpdateZoneRequest(@NotBlank String name) {
}
