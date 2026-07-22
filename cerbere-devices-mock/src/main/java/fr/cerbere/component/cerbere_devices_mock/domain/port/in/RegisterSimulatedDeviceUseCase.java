package fr.cerbere.component.cerbere_devices_mock.domain.port.in;

import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;

import java.util.UUID;

/**
 * Port d'entrée : enregistrer un nouveau device simulé.
 */
public interface RegisterSimulatedDeviceUseCase {

    SimulatedDevice register(UUID id, DeviceType type, String label, UUID zoneId, boolean autoSimulate);

}
