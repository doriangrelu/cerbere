package fr.cerbere.component.cerbere_history.domain.port.in.alarm;

import fr.cerbere.component.cerbere_history.domain.model.AlertRecord;

/**
 * Port d'entrée : historiser une alerte levée.
 */
public interface RecordAlertUseCase {

	void record(AlertRecord alert);
}
