package fr.cerbere.component.cerbere_devices_bridge.domain.port.in;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.BridgedDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;

import java.util.UUID;

/**
 * Port d'entrée : enregistrer le miroir local d'un nouveau device physique,
 * suite à un {@code device.created} reçu du registre officiel.
 */
public interface RegisterBridgedDeviceUseCase {

	BridgedDevice register(UUID id, DeviceType type, String label, UUID zoneId);
}
