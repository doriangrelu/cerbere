package fr.cerbere.component.cerbere_devices_bridge.domain.model;

/**
 * État de l'actionneur sirène — une prise/relais Zigbee générique, pas un
 * appareil "sirène" dédié : contrat {@code state: ON/OFF} symétrique en
 * lecture comme en écriture (voir docs/architecture/mqtt-zigbee-contracts.md).
 */
public enum SirenState implements DeviceState {
	ACTIVE,
	INACTIVE
}
