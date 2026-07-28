package fr.cerbere.component.cerbere_devices_bridge.adapter.in.web.dto;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DiscoveredDevice;
import fr.cerbere.shared.dto.devicebridge.DiscoveredDeviceResponse;

/**
 * Traduction entre le modèle de domaine {@link DiscoveredDevice} et le DTO REST.
 */
public final class DiscoveredDeviceWebMapper {

	private DiscoveredDeviceWebMapper() {
	}

	public static DiscoveredDeviceResponse toResponse(final DiscoveredDevice device) {
		final DeviceType inferredType = device.getInferredType();
		return new DiscoveredDeviceResponse(
			device.getFriendlyName(),
			inferredType != null ? inferredType.name() : null,
			device.getLastSeenAt()
		);
	}
}
