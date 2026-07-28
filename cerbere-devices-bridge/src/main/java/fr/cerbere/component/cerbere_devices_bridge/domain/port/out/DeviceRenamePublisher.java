package fr.cerbere.component.cerbere_devices_bridge.domain.port.out;

/**
 * Port de sortie : demande de renommage du {@code friendly_name} d'un device
 * auprès de la passerelle Zigbee2MQTT (ou du Mock qui en joue le rôle, voir
 * ADR 0021/0023). C'est le geste d'appairage : renommer un device en l'id du
 * device officiel auquel il doit correspondre, exactement comme un opérateur le
 * ferait dans l'interface Zigbee2MQTT — voir docs/architecture/mqtt-zigbee-contracts.md.
 */
public interface DeviceRenamePublisher {

	void rename(String currentFriendlyName, String newFriendlyName);
}
