package fr.cerbere.component.cerbere_core.domain.port.in.alarm;

/**
 * Réévalue si le système doit se déclencher suite à un changement qui peut
 * faire ressortir une violation déjà connue sous un nouveau jour (ex : un
 * device repassé actif alors qu'il était déjà en violation pendant qu'il
 * était inactif) — sans changer le mode d'armement courant. No-op si le
 * système est désarmé ou déjà déclenché.
 */
public interface ReevaluateAlarmTriggerUseCase {

	void reevaluate();
}
