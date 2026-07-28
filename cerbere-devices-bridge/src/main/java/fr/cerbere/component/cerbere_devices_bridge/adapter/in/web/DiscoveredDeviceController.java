package fr.cerbere.component.cerbere_devices_bridge.adapter.in.web;

import fr.cerbere.component.cerbere_devices_bridge.adapter.in.web.dto.DiscoveredDeviceWebMapper;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.ListDiscoveredDevicesUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.PairDiscoveredDeviceUseCase;
import fr.cerbere.shared.dto.devicebridge.DiscoveredDeviceResponse;
import fr.cerbere.shared.dto.devicebridge.PairDiscoveredDeviceRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * API REST d'appairage (voir ADR 0023) : liste les devices vus sur MQTT en
 * attente de rattachement, et déclenche l'appairage (renommage du
 * {@code friendly_name} auprès de la passerelle Zigbee2MQTT). Consommée par
 * l'écran Appairage de {@code cerbere-bff}. Fonctionne à l'identique avec du
 * matériel réel et avec {@code cerbere-devices-mock} (voir ADR 0021).
 */
@RestController
@RequestMapping("/api/devices-bridge/discovered-devices")
@RequiredArgsConstructor
public class DiscoveredDeviceController {

	private final ListDiscoveredDevicesUseCase listDiscoveredDevicesUseCase;
	private final PairDiscoveredDeviceUseCase pairDiscoveredDeviceUseCase;

	@GetMapping
	public List<DiscoveredDeviceResponse> listAll() {
		return this.listDiscoveredDevicesUseCase.listAll().stream()
			.map(DiscoveredDeviceWebMapper::toResponse)
			.toList();
	}

	@PostMapping("/pair")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void pair(@Valid @RequestBody final PairDiscoveredDeviceRequest request) {
		this.pairDiscoveredDeviceUseCase.pair(request.friendlyName(), UUID.fromString(request.coreDeviceId()));
	}
}
