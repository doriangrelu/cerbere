package fr.cerbere.component.cerbere_core.infrastructure.messaging.spring;

import fr.cerbere.component.cerbere_core.domain.event.DeviceSupervisionChanged;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceSupervisionChangedPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adapter du port {@link DeviceSupervisionChangedPublisher} vers le bus
 * applicatif Spring. Seule classe du module à connaître
 * {@link ApplicationEventPublisher} : la couche {@code application} reste
 * dépourvue de type framework, exactement comme pour les producteurs Kafka —
 * voir ADR 0025.
 */
@Component
@RequiredArgsConstructor
public class SpringDeviceSupervisionChangedPublisher implements DeviceSupervisionChangedPublisher {

	private final ApplicationEventPublisher applicationEventPublisher;

	@Override
	public void publish(final DeviceSupervisionChanged event) {
		this.applicationEventPublisher.publishEvent(event);
	}
}
