package fr.cerbere.component.cerbere_devices_mock.infrastructure.persistence.mongo;

import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;

import java.util.UUID;

/**
 * Traduction entre le modèle de domaine {@link SimulatedDevice} et sa représentation Mongo.
 */
final class SimulatedDeviceMapper {

	private SimulatedDeviceMapper() {
	}

	static SimulatedDeviceDocument toDocument(final SimulatedDevice device) {
		final DeviceState state = device.getCurrentState();
		final UUID zoneId = device.getZoneId();
		return new SimulatedDeviceDocument(
			device.getId().toString(),
			device.getType().name(),
			device.getLabel(),
			zoneId != null ? zoneId.toString() : null,
			device.isAutoSimulate(),
			state.name()
		);
	}

	static SimulatedDevice toDomain(final SimulatedDeviceDocument document) {
		final DeviceType type = DeviceType.valueOf(document.type());
		final DeviceState state = type.parseState(document.state());
		final String zoneId = document.zoneId();
		return SimulatedDevice.restore(
			UUID.fromString(document.id()),
			type,
			document.label(),
			zoneId != null ? UUID.fromString(zoneId) : null,
			document.autoSimulate(),
			state
		);
	}
}
