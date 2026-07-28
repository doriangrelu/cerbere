package fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.mqtt.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Contrat de la requête de renommage de l'API bridge de Zigbee2MQTT, reçue sur
 * {@code <base-topic>/bridge/request/device/rename} — le Mock en joue le rôle
 * (voir ADR 0021/0023). Copie locale identique à celle de
 * {@code cerbere-devices-bridge} : les deux modules parlent le même protocole
 * MQTT sans partager de code, comme un vrai device et son intégration.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DeviceRenameRequest(String from, String to) {
}
