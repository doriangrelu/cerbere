package fr.cerbere.component.cerbere_core.domain.port.out.alarm;

import fr.cerbere.component.cerbere_core.domain.event.AlarmStateChanged;

/**
 * Port de sortie : publication d'un changement d'état de l'alarme vers
 * {@code cerbere.alarm.state-changed}.
 */
public interface AlarmStateChangedPublisher {

	void publish(AlarmStateChanged event);
}
