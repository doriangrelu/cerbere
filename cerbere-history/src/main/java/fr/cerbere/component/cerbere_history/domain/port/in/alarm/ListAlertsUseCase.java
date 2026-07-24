package fr.cerbere.component.cerbere_history.domain.port.in.alarm;

import fr.cerbere.component.cerbere_history.domain.model.AlertRecord;
import fr.cerbere.component.cerbere_history.domain.model.AlertSeverity;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;

import java.time.Instant;
import java.util.UUID;

/**
 * Port d'entrée : consulter l'historique des alertes, filtré par
 * device/zone/sévérité/plage temporelle (chaque filtre est optionnel).
 */
public interface ListAlertsUseCase {

	PageResult<AlertRecord> list(UUID deviceId, UUID zoneId, AlertSeverity severity, Instant from, Instant to, PageRequest pageRequest);
}
