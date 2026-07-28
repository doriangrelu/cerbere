package fr.cerbere.component.cerbere_devices_mock.adapter.in.scheduler;

import fr.cerbere.component.cerbere_devices_mock.domain.port.in.EmitDeviceHeartbeatsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Fait rapporter périodiquement leur état aux devices simulés joignables, comme
 * le font les vrais capteurs Zigbee (voir ADR 0024). Remplace l'ancien scheduler
 * de simulation aléatoire : un capteur ne change pas d'état au hasard, il répète
 * ce qu'il observe.
 */
@Component
@RequiredArgsConstructor
public final class DeviceHeartbeatScheduler {

	private final EmitDeviceHeartbeatsUseCase emitDeviceHeartbeatsUseCase;

	@Scheduled(fixedDelayString = "${cerbere.devices-mock.heartbeat.fixed-delay-ms}")
	public void emitHeartbeats() {
		this.emitDeviceHeartbeatsUseCase.emitAll();
	}
}
