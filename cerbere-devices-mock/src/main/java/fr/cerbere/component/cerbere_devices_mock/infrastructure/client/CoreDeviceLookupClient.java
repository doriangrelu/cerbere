package fr.cerbere.component.cerbere_devices_mock.infrastructure.client;

import fr.cerbere.component.cerbere_devices_mock.domain.port.out.CoreDeviceLookupPort;
import fr.cerbere.shared.dto.device.DeviceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation REST du port {@link CoreDeviceLookupPort} : interroge
 * {@code GET /api/devices} sur {@code cerbere-core} et filtre par id (pas de
 * endpoint dédié get-by-id côté core pour l'instant, la liste suffit à cette échelle).
 */
@Component
@RequiredArgsConstructor
public final class CoreDeviceLookupClient implements CoreDeviceLookupPort {

	private final RestClient coreRestClient;

	@Override
	public Optional<String> findTypeById(final UUID deviceId) {
		final List<DeviceResponse> devices = this.coreRestClient.get()
			.uri("/api/devices")
			.retrieve()
			.body(new ParameterizedTypeReference<List<DeviceResponse>>() {
			});
		return devices.stream()
			.filter(device -> device.id().equals(deviceId.toString()))
			.map(DeviceResponse::type)
			.findFirst();
	}
}
