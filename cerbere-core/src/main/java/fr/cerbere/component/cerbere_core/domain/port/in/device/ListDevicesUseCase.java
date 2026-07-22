package fr.cerbere.component.cerbere_core.domain.port.in.device;

import fr.cerbere.component.cerbere_core.domain.model.Device;

import java.util.List;

/**
 * Port d'entrée : lister les devices du registre officiel.
 */
public interface ListDevicesUseCase {

	List<Device> listAll();
}
