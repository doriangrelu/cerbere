package fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.mqtt.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Contrat du payload MQTT d'une prise/relais Zigbee générique (utilisée pour
 * piloter la sirène) : symétrique en lecture (état rapporté) comme en écriture
 * (commande envoyée sur {@code .../set}). Voir
 * docs/architecture/mqtt-zigbee-contracts.md.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SwitchState(String state) {

	public static final String ON = "ON";
	public static final String OFF = "OFF";
}
