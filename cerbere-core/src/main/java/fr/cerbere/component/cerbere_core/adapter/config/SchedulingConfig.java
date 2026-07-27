package fr.cerbere.component.cerbere_core.adapter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Active le support {@code @Scheduled} requis par {@code DeviceHeartbeatScheduler}.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
public final class SchedulingConfig {
}
