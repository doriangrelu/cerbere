package fr.cerbere.component.cerbere_bff.client.devicemock;

import fr.cerbere.shared.dto.devicemock.DeviceEventResponse;
import fr.cerbere.shared.dto.devicemock.SimulatedDeviceResponse;
import fr.cerbere.shared.dto.devicemock.TriggerDeviceEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Client REST vers les endpoints {@code /api/devices-mock/*} de
 * {@code cerbere-devices-mock}, utilisé par la section "mode test" du BFF
 * pour piloter les devices simulés. DTOs partagés via
 * {@code cerbere-shared-kernel} (voir ADR 0010/0013).
 */
@Component
@RequiredArgsConstructor
public final class DeviceMockClient {

	private final RestClient devicesMockRestClient;

	public List<SimulatedDeviceResponse> listAll() {
		return this.devicesMockRestClient.get()
			.uri("/api/devices-mock")
			.retrieve()
			.body(new ParameterizedTypeReference<List<SimulatedDeviceResponse>>() {
			});
	}

	public DeviceEventResponse triggerEvent(final String deviceId, final String state) {
		return this.devicesMockRestClient.post()
			.uri("/api/devices-mock/{deviceId}/events", deviceId)
			.body(new TriggerDeviceEventRequest(state))
			.retrieve()
			.body(DeviceEventResponse.class);
	}
}
