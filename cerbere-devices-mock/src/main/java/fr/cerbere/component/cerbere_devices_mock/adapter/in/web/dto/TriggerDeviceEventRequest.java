package fr.cerbere.component.cerbere_devices_mock.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête de déclenchement manuel d'un événement. {@code state} doit correspondre
 * à une constante de l'énumération d'état du type du device ciblé (ex: OPEN/CLOSED
 * pour un contact).
 */
public record TriggerDeviceEventRequest(@NotBlank String state) {
}
