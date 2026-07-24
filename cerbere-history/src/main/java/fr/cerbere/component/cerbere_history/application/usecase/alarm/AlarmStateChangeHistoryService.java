package fr.cerbere.component.cerbere_history.application.usecase.alarm;

import fr.cerbere.component.cerbere_history.domain.model.AlarmStateChangeRecord;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;
import fr.cerbere.component.cerbere_history.domain.port.in.alarm.ListAlarmStateChangesUseCase;
import fr.cerbere.component.cerbere_history.domain.port.in.alarm.RecordAlarmStateChangeUseCase;
import fr.cerbere.component.cerbere_history.domain.port.out.alarm.AlarmStateChangeRecordRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

/**
 * Implémentation des use-cases d'historisation/consultation des changements
 * d'état de l'alarme, regroupés car ils partagent le même port de sortie.
 */
@RequiredArgsConstructor
public final class AlarmStateChangeHistoryService implements RecordAlarmStateChangeUseCase, ListAlarmStateChangesUseCase {

	private final AlarmStateChangeRecordRepository alarmStateChangeRecordRepository;

	@Override
	public void record(final AlarmStateChangeRecord change) {
		this.alarmStateChangeRecordRepository.save(change);
	}

	@Override
	public PageResult<AlarmStateChangeRecord> list(final Instant from, final Instant to, final PageRequest pageRequest) {
		return this.alarmStateChangeRecordRepository.search(from, to, pageRequest);
	}
}
