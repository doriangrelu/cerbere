package fr.cerbere.component.cerbere_devices_bridge.domain.port.in;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;

/**
 * Port d'entrée : enregistrer (ou rafraîchir) un device vu sur MQTT dont le
 * {@code friendly_name} ne correspond à aucun device connu — candidat à
 * l'appairage (voir ADR 0023). Appelé directement par l'adapter MQTT
 * ({@code ZigbeeDeviceStateMqttListener}), c'est donc un véritable port
 * d'entrée au sens de l'ADR 0018.
 */
public interface RecordDiscoveredDeviceUseCase {

	void record(String friendlyName, DeviceType inferredType);
}
