package fr.cerbere.component.cerbere_core.domain.exception;

/**
 * Levée quand la sauvegarde de l'agrégat {@code AlarmSystem} entre en conflit
 * avec une écriture concurrente (verrouillage optimiste Mongo). {@code AlarmSystem}
 * est un document unique ({@code home-1}) écrit à la fois par le thread du
 * scheduler de supervision de vie et par celui qui traite chaque événement de
 * device (voir ADR 0025) : deux écritures concurrentes sur ce document sont
 * attendues, pas exceptionnelles — l'appelant doit relire l'état courant et
 * retenter sa décision plutôt que d'abandonner. Traduit depuis
 * {@code OptimisticLockingFailureException} à la frontière de l'adapter Mongo,
 * pour que la couche {@code application} n'ait aucune dépendance à Spring Data
 * (voir ADR 0001).
 */
public final class ConcurrentAlarmSystemUpdateException extends RuntimeException {

	public ConcurrentAlarmSystemUpdateException(final String systemId, final Throwable cause) {
		super("Concurrent update detected while saving alarm system: " + systemId, cause);
	}
}
