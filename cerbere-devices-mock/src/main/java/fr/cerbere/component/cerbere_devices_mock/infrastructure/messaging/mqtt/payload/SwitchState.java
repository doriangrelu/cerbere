package fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.mqtt.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Contrat du payload MQTT d'une prise/relais Zigbee générique (sirène) —
 * identique à celui d'un vrai device Zigbee2MQTT, symétrique en lecture (état
 * rapporté) comme en écriture (commande reçue sur {@code .../set}) — voir
 * docs/architecture/mqtt-zigbee-contracts.md et ADR 0021.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SwitchState(String state) {

	public static final String ON = "ON";
	public static final String OFF = "OFF";
}
