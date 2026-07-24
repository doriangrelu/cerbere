package fr.cerbere.component.cerbere_history.domain.port.out.alarm;

import fr.cerbere.component.cerbere_history.domain.model.AlarmStateChangeRecord;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;

import java.time.Instant;

/**
 * Port de sortie : persistance de l'historique des changements d'état de l'alarme.
 */
public interface AlarmStateChangeRecordRepository {

	AlarmStateChangeRecord save(AlarmStateChangeRecord change);

	PageResult<AlarmStateChangeRecord> search(Instant from, Instant to, PageRequest pageRequest);
}
