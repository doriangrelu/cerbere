package fr.cerbere.component.cerbere_devices_bridge.infrastructure.persistence.mongo;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.BridgedDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceState;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;

import java.util.UUID;

/**
 * Traduction entre le modèle de domaine {@link BridgedDevice} et sa représentation Mongo.
 */
final class BridgedDeviceMapper {

	private BridgedDeviceMapper() {
	}

	static BridgedDeviceDocument toDocument(final BridgedDevice device) {
		final UUID zoneId = device.getZoneId();
		return new BridgedDeviceDocument(
			device.getId().toString(),
			device.getType().name(),
			device.getLabel(),
			zoneId != null ? zoneId.toString() : null,
			device.getLastKnownState().name(),
			device.getVersion()
		);
	}

	static BridgedDevice toDomain(final BridgedDeviceDocument document) {
		final DeviceType type = DeviceType.valueOf(document.type());
		final DeviceState state = type.parseState(document.state());
		final String zoneId = document.zoneId();
		return BridgedDevice.restore(
			UUID.fromString(document.id()),
			type,
			document.label(),
			zoneId != null ? UUID.fromString(zoneId) : null,
			state,
			document.version()
		);
	}
}
