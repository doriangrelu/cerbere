package fr.cerbere.component.cerbere_core.domain.port.in.device;

import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.DeviceType;

import java.util.UUID;

/**
 * Port d'entrée : enregistrer un nouveau device dans le registre officiel.
 */
public interface RegisterDeviceUseCase {

	Device register(DeviceType type, String label, UUID zoneId);
}
