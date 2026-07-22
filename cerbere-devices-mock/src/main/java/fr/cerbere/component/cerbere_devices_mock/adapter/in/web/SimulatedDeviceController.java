package fr.cerbere.component.cerbere_devices_mock.adapter.in.web;

import fr.cerbere.component.cerbere_devices_mock.adapter.in.web.dto.RegisterSimulatedDeviceRequest;
import fr.cerbere.component.cerbere_devices_mock.adapter.in.web.dto.SimulatedDeviceResponse;
import fr.cerbere.component.cerbere_devices_mock.adapter.in.web.dto.SimulatedDeviceWebMapper;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.ListSimulatedDevicesUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.RegisterSimulatedDeviceUseCase;
import jakarta.validation.Valid;
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
 * API REST de gestion des devices simulés (CRUD minimal : créer, lister).
 */
@RestController
@RequestMapping("/api/devices-mock")
public final class SimulatedDeviceController {

	private final RegisterSimulatedDeviceUseCase registerSimulatedDeviceUseCase;
	private final ListSimulatedDevicesUseCase listSimulatedDevicesUseCase;

	public SimulatedDeviceController(final RegisterSimulatedDeviceUseCase registerSimulatedDeviceUseCase,
									  final ListSimulatedDevicesUseCase listSimulatedDevicesUseCase) {
		this.registerSimulatedDeviceUseCase = registerSimulatedDeviceUseCase;
		this.listSimulatedDevicesUseCase = listSimulatedDevicesUseCase;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SimulatedDeviceResponse register(@Valid @RequestBody final RegisterSimulatedDeviceRequest request) {
		final DeviceType type = DeviceType.valueOf(request.type());
		final UUID zoneId = request.zoneId() != null ? UUID.fromString(request.zoneId()) : null;
		final SimulatedDevice device = this.registerSimulatedDeviceUseCase.register(type, request.label(), zoneId, request.autoSimulate());
		return SimulatedDeviceWebMapper.toResponse(device);
	}

	@GetMapping
	public List<SimulatedDeviceResponse> listAll() {
		return this.listSimulatedDevicesUseCase.listAll().stream()
			.map(SimulatedDeviceWebMapper::toResponse)
			.toList();
	}
}
