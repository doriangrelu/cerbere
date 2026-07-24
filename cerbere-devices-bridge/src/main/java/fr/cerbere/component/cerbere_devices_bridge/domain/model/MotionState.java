package fr.cerbere.component.cerbere_devices_bridge.domain.model;

/**
 * État d'un détecteur de mouvement (Aqara RTCGQ11LM : {@code occupancy:true}
 * = mouvement détecté).
 */
public enum MotionState implements DeviceState {
	DETECTED,
	CLEAR
}
