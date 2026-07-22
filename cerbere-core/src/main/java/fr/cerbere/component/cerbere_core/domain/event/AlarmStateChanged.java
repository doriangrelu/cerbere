package fr.cerbere.component.cerbere_core.domain.event;

import fr.cerbere.component.cerbere_core.domain.model.AlarmMode;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine émis à chaque transition du mode d'armement (arm,
 * disarm, ou déclenchement) — publié sur {@code cerbere.alarm.state-changed}.
 */
public record AlarmStateChanged(
	String systemId,
	AlarmMode previousMode,
	AlarmMode newMode,
	boolean triggered,
	Instant occurredAt,
	UUID correlationId
) {
}
