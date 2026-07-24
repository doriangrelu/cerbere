package fr.cerbere.component.cerbere_devices_bridge.application.usecase;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.BridgedDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.RegisterBridgedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.BridgedDeviceRepository;

import java.util.UUID;

/**
 * Implémentation du use-case d'enregistrement du miroir local d'un device physique.
 */
public final class RegisterBridgedDeviceService implements RegisterBridgedDeviceUseCase {

	private final BridgedDeviceRepository bridgedDeviceRepository;

	public RegisterBridgedDeviceService(final BridgedDeviceRepository bridgedDeviceRepository) {
		this.bridgedDeviceRepository = bridgedDeviceRepository;
	}

	@Override
	public BridgedDevice register(final UUID id, final DeviceType type, final String label, final UUID zoneId) {
		final BridgedDevice device = BridgedDevice.register(id, type, label, zoneId);
		return this.bridgedDeviceRepository.save(device);
	}
}
