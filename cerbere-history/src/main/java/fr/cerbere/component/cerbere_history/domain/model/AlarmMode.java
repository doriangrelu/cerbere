package fr.cerbere.component.cerbere_history.domain.model;

/**
 * Mode d'armement tel qu'historisé, miroir des valeurs échangées via
 * {@code cerbere.alarm.state-changed}. Copie locale volontaire : {@code cerbere-history}
 * n'importe jamais le domaine de {@code cerbere-core} (frontière inter-services
 * stricte, même principe que le database-per-service de l'ADR 0003).
 */
public enum AlarmMode {
	DISARMED,
	ARMED_AWAY,
	ARMED_HOME
}
