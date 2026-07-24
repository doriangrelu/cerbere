package fr.cerbere.component.cerbere_devices_bridge.adapter.config;

import fr.cerbere.component.cerbere_devices_bridge.application.usecase.CommandSirenService;
import fr.cerbere.component.cerbere_devices_bridge.application.usecase.DeleteBridgedDeviceService;
import fr.cerbere.component.cerbere_devices_bridge.application.usecase.ListBridgedDevicesService;
import fr.cerbere.component.cerbere_devices_bridge.application.usecase.RegisterBridgedDeviceService;
import fr.cerbere.component.cerbere_devices_bridge.application.usecase.ReportDeviceStateService;
import fr.cerbere.component.cerbere_devices_bridge.application.usecase.UpdateBridgedDeviceService;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.CommandSirenUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.DeleteBridgedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.ListBridgedDevicesUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.RegisterBridgedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.ReportDeviceStateUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.UpdateBridgedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.BridgedDeviceRepository;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DeviceCommandPublisher;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DeviceEventPublisher;
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
	public RegisterBridgedDeviceUseCase registerBridgedDeviceUseCase(final BridgedDeviceRepository bridgedDeviceRepository) {
		return new RegisterBridgedDeviceService(bridgedDeviceRepository);
	}

	@Bean
	public UpdateBridgedDeviceUseCase updateBridgedDeviceUseCase(final BridgedDeviceRepository bridgedDeviceRepository) {
		return new UpdateBridgedDeviceService(bridgedDeviceRepository);
	}

	@Bean
	public DeleteBridgedDeviceUseCase deleteBridgedDeviceUseCase(final BridgedDeviceRepository bridgedDeviceRepository) {
		return new DeleteBridgedDeviceService(bridgedDeviceRepository);
	}

	@Bean
	public ListBridgedDevicesUseCase listBridgedDevicesUseCase(final BridgedDeviceRepository bridgedDeviceRepository) {
		return new ListBridgedDevicesService(bridgedDeviceRepository);
	}

	@Bean
	public ReportDeviceStateUseCase reportDeviceStateUseCase(final BridgedDeviceRepository bridgedDeviceRepository,
															   final DeviceEventPublisher deviceEventPublisher) {
		return new ReportDeviceStateService(bridgedDeviceRepository, deviceEventPublisher);
	}

	@Bean
	public CommandSirenUseCase commandSirenUseCase(final BridgedDeviceRepository bridgedDeviceRepository,
													 final DeviceCommandPublisher deviceCommandPublisher) {
		return new CommandSirenService(bridgedDeviceRepository, deviceCommandPublisher);
	}
}
