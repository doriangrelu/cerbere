package fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.mqtt.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Contrat du payload MQTT publié par un détecteur de mouvement (référence :
 * Aqara RTCGQ11LM). {@code occupancy:true} = mouvement détecté. Voir
 * docs/architecture/mqtt-zigbee-contracts.md pour un exemple réel.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MotionSensorPayload(
	Boolean occupancy,
	Integer illuminance,
	Integer battery,
	Integer voltage
) {
}
