package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.port.in.DeleteSimulatedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;

import java.util.UUID;

/**
 * Implémentation du use-case de retrait du miroir local d'un device simulé.
 * Idempotent : silencieux si le device n'existe déjà plus localement — voir ADR 0016.
 */
public final class DeleteSimulatedDeviceService implements DeleteSimulatedDeviceUseCase {

	private final SimulatedDeviceRepository simulatedDeviceRepository;

	public DeleteSimulatedDeviceService(final SimulatedDeviceRepository simulatedDeviceRepository) {
		this.simulatedDeviceRepository = simulatedDeviceRepository;
	}

	@Override
	public void delete(final UUID id) {
		this.simulatedDeviceRepository.deleteById(id);
	}
}
