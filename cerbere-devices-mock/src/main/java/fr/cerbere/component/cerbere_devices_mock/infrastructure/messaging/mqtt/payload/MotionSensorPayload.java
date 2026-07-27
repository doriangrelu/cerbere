package fr.cerbere.component.cerbere_devices_mock.infrastructure.messaging.mqtt.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Contrat du payload MQTT publié par un détecteur de mouvement — identique à
 * celui d'un vrai device Zigbee2MQTT (référence : Aqara RTCGQ11LM, voir
 * docs/architecture/mqtt-zigbee-contracts.md), voir ADR 0021.
 * {@code occupancy:true} = mouvement détecté.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MotionSensorPayload(
	Boolean occupancy,
	Integer illuminance,
	Integer battery,
	Integer voltage
) {
}
