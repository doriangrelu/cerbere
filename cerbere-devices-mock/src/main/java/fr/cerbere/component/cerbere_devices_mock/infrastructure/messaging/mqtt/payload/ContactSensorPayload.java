package fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.mqtt.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contrat du payload MQTT publié par un capteur de contact porte/fenêtre —
 * identique à celui d'un vrai device Zigbee2MQTT (référence : Aqara MCCGQ11LM,
 * voir docs/architecture/mqtt-zigbee-contracts.md), pour que
 * {@code cerbere-devices-bridge} ne fasse aucune différence entre le Mock et
 * du matériel réel (voir ADR 0021). {@code contact:true} = fermé, {@code false} = ouvert.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ContactSensorPayload(
	Boolean contact,
	Integer battery,
	Integer voltage,
	@JsonProperty("device_temperature") Double deviceTemperature
) {
}
