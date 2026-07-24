package fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.mqtt.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contrat du payload MQTT publié par un capteur de contact porte/fenêtre
 * (référence : Aqara MCCGQ11LM). {@code contact:true} = fermé, {@code false} =
 * ouvert. Voir docs/architecture/mqtt-zigbee-contracts.md pour un exemple réel.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ContactSensorPayload(
	Boolean contact,
	Integer battery,
	Integer voltage,
	@JsonProperty("device_temperature") Double deviceTemperature
) {
}
