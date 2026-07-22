package fr.cerbere.shared.dto.devicemock;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête de déclenchement manuel d'un événement. {@code state} doit
 * correspondre à une constante de l'énumération d'état du type du device
 * ciblé (ex : OPEN/CLOSED pour un contact). Contrat REST partagé entre
 * {@code cerbere-bff} (émetteur) et {@code cerbere-devices-mock} (récepteur).
 */
public record TriggerDeviceEventRequest(@NotBlank String state) {
}
