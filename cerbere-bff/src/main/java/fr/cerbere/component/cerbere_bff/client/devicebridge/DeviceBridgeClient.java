package fr.cerbere.component.cerbere_bff.client.devicebridge;

import fr.cerbere.shared.dto.devicebridge.DiscoveredDeviceResponse;
import fr.cerbere.shared.dto.devicebridge.PairDiscoveredDeviceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Client REST vers les endpoints {@code /api/devices-bridge/*} de
 * {@code cerbere-devices-bridge}, utilisé par l'écran Appairage du BFF. Le
 * Bridge est le seul canal d'appairage (voir ADR 0023), qu'il s'agisse de
 * matériel Zigbee réel ou du Mock : le BFF n'a donc jamais à savoir lequel des
 * deux tourne. DTOs partagés via {@code cerbere-shared-kernel} (ADR 0010/0013).
 */
@Component
@RequiredArgsConstructor
public class DeviceBridgeClient {

	private final RestClient devicesBridgeRestClient;

	public List<DiscoveredDeviceResponse> listDiscoveredDevices() {
		return this.devicesBridgeRestClient.get()
			.uri("/api/devices-bridge/discovered-devices")
			.retrieve()
			.body(new ParameterizedTypeReference<List<DiscoveredDeviceResponse>>() {
			});
	}

	public void pair(final String friendlyName, final String coreDeviceId) {
		this.devicesBridgeRestClient.post()
			.uri("/api/devices-bridge/discovered-devices/pair")
			.body(new PairDiscoveredDeviceRequest(friendlyName, coreDeviceId))
			.retrieve()
			.toBodilessEntity();
	}
}
