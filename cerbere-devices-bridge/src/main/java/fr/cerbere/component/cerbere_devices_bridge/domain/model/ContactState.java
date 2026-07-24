package fr.cerbere.component.cerbere_devices_bridge.domain.model;

/**
 * État d'un capteur de contact porte/fenêtre (Aqara MCCGQ11LM : {@code contact:true}
 * = fermé, {@code false} = ouvert — voir docs/architecture/mqtt-zigbee-contracts.md).
 */
public enum ContactState implements DeviceState {
	OPEN,
	CLOSED
}
