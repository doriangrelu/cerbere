package fr.cerbere.component.cerbere_core.application.usecase.alarm;

import fr.cerbere.component.cerbere_core.application.service.AlarmTriggerReevaluationService;
import fr.cerbere.component.cerbere_core.application.service.RecomputeZoneViolationService;
import fr.cerbere.component.cerbere_core.domain.event.DeviceSupervisionChanged;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.ReevaluateAlarmStateUseCase;
import lombok.RequiredArgsConstructor;

/**
 * Seul endroit du module qui dérive l'état d'alarme de l'état des devices.
 * Les use-cases qui modifient un device (événement capteur, supervision de vie,
 * modification, suppression) se contentent d'émettre
 * {@link DeviceSupervisionChanged} : ils n'ont plus à connaître ni à répéter cet
 * enchaînement — voir ADR 0025.
 * <p>
 * L'ordre est significatif : la violation des zones est recalculée d'abord, pour
 * que la réévaluation du déclenchement observe un état cohérent. La réévaluation
 * est inconditionnelle et idempotente ({@code AlarmTriggerReevaluationService}
 * ne fait rien si le système est désarmé ou déjà déclenché), ce qui évite à
 * l'appelant d'avoir à juger si son changement « méritait » une réévaluation.
 */
@RequiredArgsConstructor
public final class ReevaluateAlarmStateService implements ReevaluateAlarmStateUseCase {

	private final RecomputeZoneViolationService recomputeZoneViolationService;
	private final AlarmTriggerReevaluationService alarmTriggerReevaluationService;

	@Override
	public void reevaluate(final DeviceSupervisionChanged event) {
		event.affectedZoneIds().forEach(this.recomputeZoneViolationService::recompute);
		this.alarmTriggerReevaluationService.reevaluate();
	}
}
