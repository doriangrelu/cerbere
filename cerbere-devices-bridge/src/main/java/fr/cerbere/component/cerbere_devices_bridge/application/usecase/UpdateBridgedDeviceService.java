package fr.cerbere.component.cerbere_devices_bridge.application.usecase;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.BridgedDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.UpdateBridgedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.BridgedDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Implémentation du use-case d'alignement d'un device physique sur le registre
 * officiel. Résiliente si le miroir local n'existe pas encore (device pas
 * encore synchronisé) : ignore plutôt que d'échouer — voir ADR 0016.
 */
public final class UpdateBridgedDeviceService implements UpdateBridgedDeviceUseCase {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateBridgedDeviceService.class);

	private final BridgedDeviceRepository bridgedDeviceRepository;

	public UpdateBridgedDeviceService(final BridgedDeviceRepository bridgedDeviceRepository) {
		this.bridgedDeviceRepository = bridgedDeviceRepository;
	}

	@Override
	public void update(final UUID id, final String label, final UUID zoneId) {
		final BridgedDevice device = this.bridgedDeviceRepository.findById(id).orElse(null);
		if (device == null) {
			LOGGER.info("Ignoring update for unknown bridged device {} (not yet synced)", id);
			return;
		}
		this.bridgedDeviceRepository.save(device.withLabelAndZone(label, zoneId));
	}
}
