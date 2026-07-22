package fr.cerbere.component.cerbere_core.domain.port.in.device;

import fr.cerbere.component.cerbere_core.domain.model.Device;

import java.util.UUID;

/**
 * Port d'entrée : modifier un device existant (libellé, zone, activation).
 * Le {@code DeviceType} n'est volontairement pas modifiable (voir {@link Device}).
 */
public interface UpdateDeviceUseCase {

	Device update(UUID id, String label, UUID zoneId, boolean enabled);
}
