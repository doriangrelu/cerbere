package fr.cerbere.component.cerbere_history.domain.port.in.alarm;

import fr.cerbere.component.cerbere_history.domain.model.AlarmStateChangeRecord;

/**
 * Port d'entrée : historiser un changement d'état de l'alarme.
 */
public interface RecordAlarmStateChangeUseCase {

	void record(AlarmStateChangeRecord change);
}
