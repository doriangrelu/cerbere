package fr.cerbere.shared.dto.devicemock;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête de renommage du {@code friendlyName} MQTT d'un device simulé — action
 * d'appairage (voir ADR 0021) : symétrique au renommage manuel du
 * {@code friendly_name} dans Zigbee2MQTT pour du matériel réel
 * (docs/architecture/mqtt-zigbee-contracts.md). Contrat REST partagé entre
 * {@code cerbere-bff} (émetteur) et {@code cerbere-devices-mock} (récepteur).
 */
public record RenameSimulatedDeviceRequest(@NotBlank String friendlyName) {
}
