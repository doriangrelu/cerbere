package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.exception.DeviceAlreadyBoundException;
import fr.cerbere.component.cerbere_devices_mock.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.BindSimulatedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;

import java.util.UUID;

/**
 * Implémentation du use-case de liaison : retire le miroir orphelin (id
 * généré par le mock) et le réinsère sous l'id du device officiel, en
 * préservant type/état courant/autoSimulate mais en adoptant label/zone du
 * device officiel (source de vérité pour ces champs, voir ADR 0016).
 */
public final class BindSimulatedDeviceService implements BindSimulatedDeviceUseCase {

	private final SimulatedDeviceRepository simulatedDeviceRepository;

	public BindSimulatedDeviceService(final SimulatedDeviceRepository simulatedDeviceRepository) {
		this.simulatedDeviceRepository = simulatedDeviceRepository;
	}

	@Override
	public SimulatedDevice bind(final UUID orphanId, final UUID coreDeviceId, final String label, final UUID zoneId) {
		final SimulatedDevice orphan = this.simulatedDeviceRepository.findById(orphanId)
			.orElseThrow(() -> new DeviceNotFoundException(orphanId));
		if (this.simulatedDeviceRepository.findById(coreDeviceId).isPresent()) {
			throw new DeviceAlreadyBoundException(coreDeviceId);
		}
		this.simulatedDeviceRepository.deleteById(orphanId);
		final SimulatedDevice bound = SimulatedDevice.restore(
			coreDeviceId, orphan.getType(), label, zoneId, orphan.isAutoSimulate(), orphan.getCurrentState(), null
		);
		return this.simulatedDeviceRepository.save(bound);
	}
}
