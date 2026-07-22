package fr.cerbere.component.cerbere_core.domain.model;

/**
 * Statut global du système, tel qu'exposé en lecture (BO, API). Dérivé de
 * {@link AlarmMode} et de l'état déclenché, jamais stocké tel quel — voir
 * {@link AlarmSystem#status()}.
 */
public enum AlarmStatus {
	DISARMED,
	ARMED_AWAY,
	ARMED_HOME,
	TRIGGERED
}
