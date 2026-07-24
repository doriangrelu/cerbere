package fr.cerbere.component.cerbere_devices_bridge.domain.port.in;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.BridgedDevice;

import java.util.List;

/**
 * Port d'entrée : lister les devices physiques connus du bridge.
 */
public interface ListBridgedDevicesUseCase {

	List<BridgedDevice> listAll();
}
