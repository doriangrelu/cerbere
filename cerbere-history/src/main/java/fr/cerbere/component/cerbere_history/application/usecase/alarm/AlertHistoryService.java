package fr.cerbere.component.cerbere_history.application.usecase.alarm;

import fr.cerbere.component.cerbere_history.domain.model.AlertRecord;
import fr.cerbere.component.cerbere_history.domain.model.AlertSeverity;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;
import fr.cerbere.component.cerbere_history.domain.port.in.alarm.ListAlertsUseCase;
import fr.cerbere.component.cerbere_history.domain.port.in.alarm.RecordAlertUseCase;
import fr.cerbere.component.cerbere_history.domain.port.out.alarm.AlertRecordRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Implémentation des use-cases d'historisation/consultation des alertes,
 * regroupés car ils partagent le même port de sortie.
 */
@RequiredArgsConstructor
public final class AlertHistoryService implements RecordAlertUseCase, ListAlertsUseCase {

	private final AlertRecordRepository alertRecordRepository;

	@Override
	public void record(final AlertRecord alert) {
		this.alertRecordRepository.save(alert);
	}

	@Override
	public PageResult<AlertRecord> list(final UUID deviceId, final UUID zoneId, final AlertSeverity severity,
										 final Instant from, final Instant to, final PageRequest pageRequest) {
		return this.alertRecordRepository.search(deviceId, zoneId, severity, from, to, pageRequest);
	}
}
