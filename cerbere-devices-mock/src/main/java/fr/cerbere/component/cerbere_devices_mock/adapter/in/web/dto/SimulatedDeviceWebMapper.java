package fr.cerbere.component.cerbere_devices_mock.adapter.in.web.dto;

import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.shared.dto.devicemock.SimulatedDeviceResponse;

/**
 * Traduction entre le modèle de domaine {@link SimulatedDevice} et les DTO REST.
 */
public final class SimulatedDeviceWebMapper {

	private SimulatedDeviceWebMapper() {
	}

	public static SimulatedDeviceResponse toResponse(final SimulatedDevice device) {
		return new SimulatedDeviceResponse(
			device.getId().toString(),
			device.getType().name(),
			device.getLabel(),
			device.getZoneId() != null ? device.getZoneId().toString() : null,
			device.isAutoSimulate(),
			device.getCurrentState().name()
		);
	}
}
