package fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.mqtt.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Contrat de la requête de renommage de l'API bridge de Zigbee2MQTT, publiée
 * sur {@code <base-topic>/bridge/request/device/rename} — voir
 * docs/architecture/mqtt-zigbee-contracts.md. C'est le geste d'appairage
 * (ADR 0023) : {@code from} est le {@code friendly_name} courant (ou l'adresse
 * IEEE), {@code to} le nouveau — côté Cerbère, l'id du device officiel.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DeviceRenameRequest(String from, String to) {
}
