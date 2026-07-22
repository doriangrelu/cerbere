package fr.cerbere.component.cerbere_core.domain.port.in.device;

import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.DeviceType;

import java.util.UUID;

/**
 * Port d'entrée : enregistrer un nouveau device dans le registre officiel.
 * {@code id} est l'identifiant du device réel/simulé côté bridge (voir
 * {@link Device#register(UUID, DeviceType, String, UUID)}), pas généré par
 * ce use-case.
 */
public interface RegisterDeviceUseCase {

	Device register(UUID id, DeviceType type, String label, UUID zoneId);
}
