package fr.cerbere.component.cerbere_bff.client.device;

import fr.cerbere.shared.dto.device.DeviceResponse;
import fr.cerbere.shared.dto.device.RegisterDeviceRequest;
import fr.cerbere.shared.dto.device.UpdateDeviceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Client REST vers les endpoints {@code /api/devices/*} de {@code cerbere-core}.
 * DTOs partagés via {@code cerbere-shared-kernel} (voir ADR 0010/0013) : pas de
 * copie locale du contrat REST.
 */
@Component
@RequiredArgsConstructor
public final class DeviceCoreClient {

	private final RestClient coreRestClient;

	public List<DeviceResponse> listAll() {
		return this.coreRestClient.get()
			.uri("/api/devices")
			.retrieve()
			.body(new ParameterizedTypeReference<List<DeviceResponse>>() {
			});
	}

	public DeviceResponse register(final RegisterDeviceRequest request) {
		return this.coreRestClient.post()
			.uri("/api/devices")
			.body(request)
			.retrieve()
			.body(DeviceResponse.class);
	}

	public DeviceResponse update(final String id, final UpdateDeviceRequest request) {
		return this.coreRestClient.put()
			.uri("/api/devices/{id}", id)
			.body(request)
			.retrieve()
			.body(DeviceResponse.class);
	}

	public void delete(final String id) {
		this.coreRestClient.delete()
			.uri("/api/devices/{id}", id)
			.retrieve()
			.toBodilessEntity();
	}
}
