package fr.cerbere.shared.dto.alarm;

import jakarta.validation.constraints.NotNull;

/**
 * Contrat REST partagé entre {@code cerbere-bff} (émetteur de la requête
 * d'armement) et {@code cerbere-core} (récepteur, {@code POST /api/alarm/arm}).
 * {@code mode} doit correspondre à une constante de {@code ArmingMode} (AWAY/HOME).
 */
public record ArmRequest(@NotNull String mode) {
}
