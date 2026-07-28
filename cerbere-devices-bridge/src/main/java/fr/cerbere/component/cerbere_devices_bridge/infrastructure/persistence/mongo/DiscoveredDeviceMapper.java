package fr.cerbere.component.cerbere_devices_bridge.infrastructure.persistence.mongo;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DiscoveredDevice;

/**
 * Traduction entre le modèle de domaine {@link DiscoveredDevice} et sa
 * représentation Mongo. {@code inferredType} peut être {@code null} (payload
 * pas encore reconnaissable), les deux sens le tolèrent.
 */
final class DiscoveredDeviceMapper {

	private DiscoveredDeviceMapper() {
	}

	static DiscoveredDeviceDocument toDocument(final DiscoveredDevice device) {
		final DeviceType inferredType = device.getInferredType();
		return new DiscoveredDeviceDocument(
			device.getFriendlyName(),
			inferredType != null ? inferredType.name() : null,
			device.getLastSeenAt(),
			device.getVersion()
		);
	}

	static DiscoveredDevice toDomain(final DiscoveredDeviceDocument document) {
		final String inferredType = document.inferredType();
		return DiscoveredDevice.restore(
			document.friendlyName(),
			inferredType != null ? DeviceType.valueOf(inferredType) : null,
			document.lastSeenAt(),
			document.version()
		);
	}
}
