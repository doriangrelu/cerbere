package fr.cerbere.component.cerbere_core.domain.port.in.alarm;

import fr.cerbere.component.cerbere_core.domain.model.AlarmSystem;
import fr.cerbere.component.cerbere_core.domain.model.ArmingMode;

/**
 * Port d'entrée : armer le système dans le mode demandé (voir {@link ArmingMode}
 * pour la signification de AWAY/HOME).
 */
public interface ArmSystemUseCase {

	AlarmSystem arm(ArmingMode mode);
}
