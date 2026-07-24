package fr.cerbere.component.cerbere_history.domain.port.in.alarm;

import fr.cerbere.component.cerbere_history.domain.model.AlarmStateChangeRecord;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;

import java.time.Instant;

/**
 * Port d'entrée : consulter l'historique des changements d'état de l'alarme,
 * filtré par plage temporelle (optionnelle).
 */
public interface ListAlarmStateChangesUseCase {

	PageResult<AlarmStateChangeRecord> list(Instant from, Instant to, PageRequest pageRequest);
}
