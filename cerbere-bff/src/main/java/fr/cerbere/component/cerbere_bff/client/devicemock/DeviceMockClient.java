package fr.cerbere.component.cerbere_bff.client.devicemock;

import fr.cerbere.shared.dto.devicemock.DeviceEventResponse;
import fr.cerbere.shared.dto.devicemock.RegisterOrphanSimulatedDeviceRequest;
import fr.cerbere.shared.dto.devicemock.SetDeviceAvailabilityRequest;
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
 * pour piloter les devices simulés. Pas de méthode d'appairage ici : celui-ci
 * passe exclusivement par {@code cerbere-devices-bridge} (voir
 * {@code DeviceBridgeClient} et ADR 0023). DTOs partagés via
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

	public SimulatedDeviceResponse registerOrphan(final String type, final String label) {
		return this.devicesMockRestClient.post()
			.uri("/api/devices-mock")
			.body(new RegisterOrphanSimulatedDeviceRequest(type, label))
			.retrieve()
			.body(SimulatedDeviceResponse.class);
	}

	public SimulatedDeviceResponse setAvailability(final String deviceId, final boolean online) {
		return this.devicesMockRestClient.put()
			.uri("/api/devices-mock/{deviceId}/availability", deviceId)
			.body(new SetDeviceAvailabilityRequest(online))
			.retrieve()
			.body(SimulatedDeviceResponse.class);
	}
}
