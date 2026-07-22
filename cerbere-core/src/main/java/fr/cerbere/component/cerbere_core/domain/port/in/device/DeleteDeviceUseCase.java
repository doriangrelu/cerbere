package fr.cerbere.component.cerbere_core.domain.port.in.device;

import java.util.UUID;

/**
 * Port d'entrée : supprimer un device du registre officiel.
 */
public interface DeleteDeviceUseCase {

	void delete(UUID id);
}
