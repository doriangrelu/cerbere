package fr.cerbere.component.cerbere_core.adapter.in.event;

import fr.cerbere.component.cerbere_core.domain.event.DeviceSupervisionChanged;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.ReevaluateAlarmStateUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Adapter d'entrée sur le bus applicatif Spring, au même titre qu'un
 * {@code @KafkaListener} ou qu'un scheduler : traduit un
 * {@link DeviceSupervisionChanged} en appel du use-case de réévaluation
 * (voir ADR 0025).
 * <p>
 * Volontairement synchrone (pas de {@code @Async}) : la réévaluation doit se
 * terminer avant que l'appelant ne rende la main, sans quoi un déclenchement
 * d'alarme pourrait arriver après la réponse HTTP ou l'accusé de réception
 * Kafka. Aucun {@code try/catch} ici non plus : une réévaluation qui échoue doit
 * faire échouer l'opération d'origine plutôt que laisser le système dans un état
 * incohérent — c'est le contraire du principe appliqué aux consumers Kafka, qui
 * eux ne doivent jamais bloquer le flux entrant.
 */
@Component
@RequiredArgsConstructor
public class DeviceSupervisionChangedListener {

	private final ReevaluateAlarmStateUseCase reevaluateAlarmStateUseCase;

	@EventListener
	public void onDeviceSupervisionChanged(final DeviceSupervisionChanged event) {
		this.reevaluateAlarmStateUseCase.reevaluate(event);
	}
}
