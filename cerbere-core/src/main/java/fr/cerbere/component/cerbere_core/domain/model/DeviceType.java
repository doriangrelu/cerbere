package fr.cerbere.component.cerbere_core.domain.model;

/**
 * Type de device du registre officiel. Volontairement distinct du {@code DeviceType}
 * de {@code cerbere-devices-mock} (bounded context séparé, voir ADR 0004) : ce
 * registre décrit les devices réels/configurés côté BO, indépendamment de toute
 * simulation.
 */
public enum DeviceType {
	CONTACT,
	MOTION,
	SIREN
}
