package fr.cerbere.component.cerbere_devices_bridge.application.usecase;

import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.DeleteBridgedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.BridgedDeviceRepository;

import java.util.UUID;

/**
 * Implémentation du use-case de retrait du miroir local d'un device physique.
 * Idempotent : silencieux si le device n'existe déjà plus localement.
 */
public final class DeleteBridgedDeviceService implements DeleteBridgedDeviceUseCase {

	private final BridgedDeviceRepository bridgedDeviceRepository;

	public DeleteBridgedDeviceService(final BridgedDeviceRepository bridgedDeviceRepository) {
		this.bridgedDeviceRepository = bridgedDeviceRepository;
	}

	@Override
	public void delete(final UUID id) {
		this.bridgedDeviceRepository.deleteById(id);
	}
}
