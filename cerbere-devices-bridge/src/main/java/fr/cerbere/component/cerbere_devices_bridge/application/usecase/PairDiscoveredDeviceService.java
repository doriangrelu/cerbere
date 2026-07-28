package fr.cerbere.component.cerbere_devices_bridge.application.usecase;

import fr.cerbere.component.cerbere_devices_bridge.domain.exception.DiscoveredDeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.PairDiscoveredDeviceUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DeviceRenamePublisher;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DiscoveredDeviceRepository;

import java.util.UUID;

/**
 * Implémentation de l'appairage (voir ADR 0023) : demande à la passerelle
 * Zigbee2MQTT (ou au Mock qui en joue le rôle) de renommer le
 * {@code friendly_name} du device en l'id du device officiel, puis retire le
 * device de la liste des candidats. Le miroir {@code BridgedDevice} n'est pas
 * créé ici : il l'est déjà par la consommation de {@code cerbere.device.state}
 * (voir ADR 0016), et c'est {@code cerbere-core} qui constatera l'appairage
 * effectif à la réception du premier événement (voir ADR 0022).
 */
public final class PairDiscoveredDeviceService implements PairDiscoveredDeviceUseCase {

	private final DiscoveredDeviceRepository discoveredDeviceRepository;
	private final DeviceRenamePublisher deviceRenamePublisher;

	public PairDiscoveredDeviceService(final DiscoveredDeviceRepository discoveredDeviceRepository,
										final DeviceRenamePublisher deviceRenamePublisher) {
		this.discoveredDeviceRepository = discoveredDeviceRepository;
		this.deviceRenamePublisher = deviceRenamePublisher;
	}

	@Override
	public void pair(final String friendlyName, final UUID coreDeviceId) {
		this.discoveredDeviceRepository.findByFriendlyName(friendlyName)
			.orElseThrow(() -> new DiscoveredDeviceNotFoundException(friendlyName));
		this.deviceRenamePublisher.rename(friendlyName, coreDeviceId.toString());
		this.discoveredDeviceRepository.deleteByFriendlyName(friendlyName);
	}
}
