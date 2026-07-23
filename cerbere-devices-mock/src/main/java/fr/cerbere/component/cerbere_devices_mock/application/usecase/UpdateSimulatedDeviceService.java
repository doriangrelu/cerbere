package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.UpdateSimulatedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Implémentation du use-case d'alignement d'un device simulé sur le registre
 * officiel. Résiliente si le miroir local n'existe pas encore (device pas
 * encore synchronisé) : ignore plutôt que d'échouer — voir ADR 0016.
 */
public final class UpdateSimulatedDeviceService implements UpdateSimulatedDeviceUseCase {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSimulatedDeviceService.class);

	private final SimulatedDeviceRepository simulatedDeviceRepository;

	public UpdateSimulatedDeviceService(final SimulatedDeviceRepository simulatedDeviceRepository) {
		this.simulatedDeviceRepository = simulatedDeviceRepository;
	}

	@Override
	public void update(final UUID id, final String label, final UUID zoneId) {
		final SimulatedDevice device = this.simulatedDeviceRepository.findById(id).orElse(null);
		if (device == null) {
			LOGGER.info("Ignoring update for unknown simulated device {} (not yet synced)", id);
			return;
		}
		this.simulatedDeviceRepository.save(device.withLabelAndZone(label, zoneId));
	}
}
