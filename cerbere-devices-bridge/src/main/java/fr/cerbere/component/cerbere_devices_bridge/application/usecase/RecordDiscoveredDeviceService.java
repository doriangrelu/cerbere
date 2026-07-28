package fr.cerbere.component.cerbere_devices_bridge.application.usecase;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DiscoveredDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.RecordDiscoveredDeviceUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DiscoveredDeviceRepository;

import java.time.Instant;

/**
 * Implémentation de l'enregistrement d'un device candidat à l'appairage
 * (voir ADR 0023). Idempotent : une observation ultérieure du même
 * {@code friendlyName} rafraîchit simplement la date de dernière vue.
 */
public final class RecordDiscoveredDeviceService implements RecordDiscoveredDeviceUseCase {

	private final DiscoveredDeviceRepository discoveredDeviceRepository;

	public RecordDiscoveredDeviceService(final DiscoveredDeviceRepository discoveredDeviceRepository) {
		this.discoveredDeviceRepository = discoveredDeviceRepository;
	}

	@Override
	public void record(final String friendlyName, final DeviceType inferredType) {
		final Instant now = Instant.now();
		final DiscoveredDevice device = this.discoveredDeviceRepository.findByFriendlyName(friendlyName)
			.map(existing -> existing.seenAgain(inferredType, now))
			.orElseGet(() -> DiscoveredDevice.discover(friendlyName, inferredType, now));
		this.discoveredDeviceRepository.save(device);
	}
}
