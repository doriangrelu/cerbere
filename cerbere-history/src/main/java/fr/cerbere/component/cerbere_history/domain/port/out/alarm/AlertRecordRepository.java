package fr.cerbere.component.cerbere_history.domain.port.out.alarm;

import fr.cerbere.component.cerbere_history.domain.model.AlertRecord;
import fr.cerbere.component.cerbere_history.domain.model.AlertSeverity;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;

import java.time.Instant;
import java.util.UUID;

/**
 * Port de sortie : persistance de l'historique des alertes.
 */
public interface AlertRecordRepository {

	AlertRecord save(AlertRecord alert);

	PageResult<AlertRecord> search(UUID deviceId, UUID zoneId, AlertSeverity severity, Instant from, Instant to, PageRequest pageRequest);
}
