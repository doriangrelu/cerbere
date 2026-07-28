package fr.cerbere.component.cerbere_core.domain.port.in.alarm;

import fr.cerbere.component.cerbere_core.domain.event.DeviceSupervisionChanged;

/**
 * Port d'entrée : réévaluer entièrement l'état d'alarme à la suite d'un
 * changement de supervision d'un device — recalcul de la violation des zones
 * concernées, puis du déclenchement du système. Unique détenteur de cet
 * enchaînement : aucun autre use-case ne doit le reproduire (voir ADR 0025).
 * <p>
 * Appelé par un adapter ({@code DeviceSupervisionChangedListener}, à l'écoute du
 * bus applicatif Spring), pas par d'autres classes {@code application} :
 * véritable port d'entrée, pas un collaborateur interne (voir ADR 0018).
 */
public interface ReevaluateAlarmStateUseCase {

	void reevaluate(DeviceSupervisionChanged event);
}
