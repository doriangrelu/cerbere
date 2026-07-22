package fr.cerbere.component.cerbere_core.domain.model;

/**
 * Mode d'armement courant, tel que stocké/dérivé par {@link AlarmSystem}. Voir
 * {@link ArmingMode} pour la signification de AWAY/HOME et pour le type utilisé
 * en entrée d'armement (qui ne contient pas {@code DISARMED}).
 */
public enum AlarmMode {
	DISARMED,
	ARMED_AWAY,
	ARMED_HOME
}
