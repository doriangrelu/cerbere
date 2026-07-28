package fr.cerbere.component.cerbere_devices_mock.adapter.config;

import fr.cerbere.component.cerbere_devices_mock.application.usecase.EmitDeviceHeartbeatsService;
import fr.cerbere.component.cerbere_devices_mock.application.usecase.ListSimulatedDevicesService;
import fr.cerbere.component.cerbere_devices_mock.application.usecase.PublishDeviceEventService;
import fr.cerbere.component.cerbere_devices_mock.application.usecase.RegisterSimulatedDeviceService;
import fr.cerbere.component.cerbere_devices_mock.application.usecase.RenameSimulatedDeviceService;
import fr.cerbere.component.cerbere_devices_mock.application.usecase.SetDeviceAvailabilityService;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.EmitDeviceHeartbeatsUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.ListSimulatedDevicesUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.RegisterSimulatedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.RenameSimulatedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.SetDeviceAvailabilityUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.TriggerDeviceEventUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.DeviceStatePublisher;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Câblage des use-cases (couche application) contre leurs ports. Les classes de
 * {@code application.usecase} restent volontairement dépourvues d'annotations Spring
 * (voir ADR 0001) : leur instanciation et leur exposition en tant que bean se font ici.
 */
@Configuration(proxyBeanMethods = false)
public class UseCaseConfig {

	@Bean
	public TriggerDeviceEventUseCase triggerDeviceEventUseCase(final SimulatedDeviceRepository simulatedDeviceRepository,
																 final DeviceStatePublisher deviceStatePublisher) {
		return new PublishDeviceEventService(simulatedDeviceRepository, deviceStatePublisher);
	}

	/**
	 * Émission périodique de l'état courant : le pendant simulé du reporting
	 * régulier d'un vrai capteur Zigbee (voir ADR 0024).
	 */
	@Bean
	public EmitDeviceHeartbeatsUseCase emitDeviceHeartbeatsUseCase(final SimulatedDeviceRepository simulatedDeviceRepository,
																	  final DeviceStatePublisher deviceStatePublisher) {
		return new EmitDeviceHeartbeatsService(simulatedDeviceRepository, deviceStatePublisher);
	}

	@Bean
	public RegisterSimulatedDeviceUseCase registerSimulatedDeviceUseCase(final SimulatedDeviceRepository simulatedDeviceRepository) {
		return new RegisterSimulatedDeviceService(simulatedDeviceRepository);
	}

	@Bean
	public ListSimulatedDevicesUseCase listSimulatedDevicesUseCase(final SimulatedDeviceRepository simulatedDeviceRepository) {
		return new ListSimulatedDevicesService(simulatedDeviceRepository);
	}

	/**
	 * Appairage : déclenché par une requête MQTT du Bridge (voir ADR 0023), plus
	 * par une API REST du Mock.
	 */
	@Bean
	public RenameSimulatedDeviceUseCase renameSimulatedDeviceUseCase(final SimulatedDeviceRepository simulatedDeviceRepository) {
		return new RenameSimulatedDeviceService(simulatedDeviceRepository);
	}

	@Bean
	public SetDeviceAvailabilityUseCase setDeviceAvailabilityUseCase(final SimulatedDeviceRepository simulatedDeviceRepository) {
		return new SetDeviceAvailabilityService(simulatedDeviceRepository);
	}
}
