package fr.cerbere.component.cerbere_core.domain.port.out.alarm;

import fr.cerbere.component.cerbere_core.domain.event.AlertRaised;

/**
 * Port de sortie : publication d'une alerte levée vers {@code cerbere.alarm.alerts}.
 */
public interface AlertPublisher {

	void publish(AlertRaised event);
}
