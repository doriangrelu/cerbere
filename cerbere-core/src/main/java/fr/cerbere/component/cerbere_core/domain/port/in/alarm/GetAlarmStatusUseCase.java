package fr.cerbere.component.cerbere_core.domain.port.in.alarm;

import fr.cerbere.component.cerbere_core.domain.model.AlarmSystem;

/**
 * Port d'entrée : consulter l'état courant du système.
 */
public interface GetAlarmStatusUseCase {

	AlarmSystem getCurrentStatus();
}
