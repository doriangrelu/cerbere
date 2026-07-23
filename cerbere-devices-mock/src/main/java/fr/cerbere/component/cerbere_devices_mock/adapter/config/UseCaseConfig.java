package fr.cerbere.component.cerbere_devices_mock.adapter.config;

import fr.cerbere.component.cerbere_devices_mock.application.usecase.DeleteSimulatedDeviceService;
import fr.cerbere.component.cerbere_devices_mock.application.usecase.ListSimulatedDevicesService;
import fr.cerbere.component.cerbere_devices_mock.application.usecase.PublishDeviceEventService;
import fr.cerbere.component.cerbere_devices_mock.application.usecase.RegisterSimulatedDeviceService;
import fr.cerbere.component.cerbere_devices_mock.application.usecase.UpdateSimulatedDeviceService;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.DeleteSimulatedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.ListSimulatedDevicesUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.RegisterSimulatedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.SimulateDeviceEventUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.TriggerDeviceEventUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.UpdateSimulatedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.DeviceEventPublisher;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Câblage des use-cases (couche application) contre leurs ports. Les classes de
 * {@code application.usecase} restent volontairement dépourvues d'annotations Spring
 * (voir ADR 0001) : leur instanciation et leur exposition en tant que bean se font ici.
 */
@Configuration(proxyBeanMethods = false)
public final class UseCaseConfig {

	@Bean
	public PublishDeviceEventService publishDeviceEventService(final SimulatedDeviceRepository simulatedDeviceRepository,
																 final DeviceEventPublisher deviceEventPublisher) {
		return new PublishDeviceEventService(simulatedDeviceRepository, deviceEventPublisher);
	}

	@Bean
	public TriggerDeviceEventUseCase triggerDeviceEventUseCase(final PublishDeviceEventService publishDeviceEventService) {
		return publishDeviceEventService;
	}

	@Bean
	public SimulateDeviceEventUseCase simulateDeviceEventUseCase(final PublishDeviceEventService publishDeviceEventService) {
		return publishDeviceEventService;
	}

	@Bean
	public RegisterSimulatedDeviceUseCase registerSimulatedDeviceUseCase(final SimulatedDeviceRepository simulatedDeviceRepository) {
		return new RegisterSimulatedDeviceService(simulatedDeviceRepository);
	}

	@Bean
	public ListSimulatedDevicesUseCase listSimulatedDevicesUseCase(final SimulatedDeviceRepository simulatedDeviceRepository) {
		return new ListSimulatedDevicesService(simulatedDeviceRepository);
	}

	@Bean
	public UpdateSimulatedDeviceUseCase updateSimulatedDeviceUseCase(final SimulatedDeviceRepository simulatedDeviceRepository) {
		return new UpdateSimulatedDeviceService(simulatedDeviceRepository);
	}

	@Bean
	public DeleteSimulatedDeviceUseCase deleteSimulatedDeviceUseCase(final SimulatedDeviceRepository simulatedDeviceRepository) {
		return new DeleteSimulatedDeviceService(simulatedDeviceRepository);
	}
}
