package fr.cerbere.component.cerbere_devices_bridge.domain.port.out;

import java.util.UUID;

/**
 * Port de sortie : envoi d'une commande à un device physique (MQTT). Contrat
 * volontairement minimal (ON/OFF) — seul le device de type SIREN (relais
 * Zigbee générique) est piloté en écriture pour cette itération.
 */
public interface DeviceCommandPublisher {

	void switchOn(UUID deviceId);

	void switchOff(UUID deviceId);
}
