package fr.cerbere.component.cerbere_core.adapter.in.scheduler;

import fr.cerbere.component.cerbere_core.domain.port.in.device.CheckDeviceHeartbeatsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Déclenche périodiquement la supervision de vie des devices (voir ADR 0020),
 * en s'appuyant sur le use-case {@link CheckDeviceHeartbeatsUseCase} — même
 * pattern que {@code DeviceSimulationScheduler} côté {@code cerbere-devices-mock}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceHeartbeatScheduler {

    private final CheckDeviceHeartbeatsUseCase checkDeviceHeartbeatsUseCase;

    @Scheduled(fixedDelayString = "${cerbere.core.device-heartbeat.check-interval-ms}")
    public void checkHeartbeats() {
        log.info("Checking device heartbeats");
        this.checkDeviceHeartbeatsUseCase.check();
    }

}
