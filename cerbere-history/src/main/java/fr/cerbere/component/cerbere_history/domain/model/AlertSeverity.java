package fr.cerbere.component.cerbere_history.domain.model;

/**
 * Sévérité d'une alerte telle qu'historisée, miroir des valeurs échangées via
 * {@code cerbere.alarm.alerts}. Copie locale volontaire, voir {@link AlarmMode}.
 */
public enum AlertSeverity {
	INFO,
	WARNING,
	CRITICAL
}
