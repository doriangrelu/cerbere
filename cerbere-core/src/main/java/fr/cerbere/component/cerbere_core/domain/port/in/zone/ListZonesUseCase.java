package fr.cerbere.component.cerbere_core.domain.port.in.zone;

import fr.cerbere.component.cerbere_core.domain.model.Zone;

import java.util.List;

/**
 * Port d'entrée : lister les zones existantes.
 */
public interface ListZonesUseCase {

	List<Zone> listAll();
}
