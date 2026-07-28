package fr.cerbere.component.cerbere_devices_bridge.application.usecase;

import fr.cerbere.component.cerbere_devices_bridge.domain.exception.DeviceTypeMismatchException;
import fr.cerbere.component.cerbere_devices_bridge.domain.exception.DiscoveredDeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.BridgedDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DiscoveredDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.PairDiscoveredDeviceUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.BridgedDeviceRepository;
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
 * <p>
 * Refuse d'associer deux types incompatibles : appairer un détecteur de
 * mouvement à une entrée déclarée comme contact ferait interpréter ses payloads
 * selon le mauvais contrat. Le contrôle s'appuie sur le miroir local du device
 * officiel ; il est volontairement permissif dans deux cas où il n'y a rien à
 * comparer — type indéterminable (payload non reconnu) et miroir pas encore
 * synchronisé (propagation Kafka en cours, voir ADR 0016).
 */
public final class PairDiscoveredDeviceService implements PairDiscoveredDeviceUseCase {

	private final DiscoveredDeviceRepository discoveredDeviceRepository;
	private final BridgedDeviceRepository bridgedDeviceRepository;
	private final DeviceRenamePublisher deviceRenamePublisher;

	public PairDiscoveredDeviceService(final DiscoveredDeviceRepository discoveredDeviceRepository,
										final BridgedDeviceRepository bridgedDeviceRepository,
										final DeviceRenamePublisher deviceRenamePublisher) {
		this.discoveredDeviceRepository = discoveredDeviceRepository;
		this.bridgedDeviceRepository = bridgedDeviceRepository;
		this.deviceRenamePublisher = deviceRenamePublisher;
	}

	@Override
	public void pair(final String friendlyName, final UUID coreDeviceId) {
		final DiscoveredDevice discovered = this.discoveredDeviceRepository.findByFriendlyName(friendlyName)
			.orElseThrow(() -> new DiscoveredDeviceNotFoundException(friendlyName));
		this.checkTypesMatch(discovered, coreDeviceId);
		this.deviceRenamePublisher.rename(friendlyName, coreDeviceId.toString());
		this.discoveredDeviceRepository.deleteByFriendlyName(friendlyName);
	}

	private void checkTypesMatch(final DiscoveredDevice discovered, final UUID coreDeviceId) {
		final DeviceType discoveredType = discovered.getInferredType();
		if (discoveredType == null) {
			return;
		}
		final BridgedDevice coreDevice = this.bridgedDeviceRepository.findById(coreDeviceId).orElse(null);
		if (coreDevice == null) {
			return;
		}
		if (coreDevice.getType() != discoveredType) {
			throw new DeviceTypeMismatchException(discovered.getFriendlyName(), discoveredType, coreDeviceId, coreDevice.getType());
		}
	}
}
