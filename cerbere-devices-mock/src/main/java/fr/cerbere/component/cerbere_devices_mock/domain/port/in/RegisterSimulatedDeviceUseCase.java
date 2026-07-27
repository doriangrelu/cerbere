package fr.cerbere.component.cerbere_devices_mock.domain.port.in;

import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;

/**
 * Port d'entrée : enregistrer un nouveau device simulé, orphelin par défaut
 * (voir ADR 0021) — id et {@code friendlyName} initial générés par le mock lui-même.
 */
public interface RegisterSimulatedDeviceUseCase {

	SimulatedDevice register(DeviceType type, String label, boolean autoSimulate);

}
