package fr.cerbere.component.cerbere_devices_mock.adapter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Active le support {@code @Scheduled} requis par {@code DeviceSimulationScheduler}.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
public final class SchedulingConfig {
}
