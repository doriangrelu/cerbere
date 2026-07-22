package fr.cerbere.component.cerbere_core.adapter.in.web.device;

import fr.cerbere.component.cerbere_core.adapter.in.web.device.dto.DeviceResponse;
import fr.cerbere.component.cerbere_core.adapter.in.web.device.dto.DeviceWebMapper;
import fr.cerbere.component.cerbere_core.adapter.in.web.device.dto.RegisterDeviceRequest;
import fr.cerbere.component.cerbere_core.adapter.in.web.device.dto.UpdateDeviceRequest;
import fr.cerbere.component.cerbere_core.adapter.in.web.mapper.CommonTextMapper;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.DeviceType;
import fr.cerbere.component.cerbere_core.domain.port.in.device.DeleteDeviceUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.device.ListDevicesUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.device.RegisterDeviceUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.device.UpdateDeviceUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * API REST de gestion du registre officiel des devices (CRUD).
 */
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public final class DeviceController {

	private final RegisterDeviceUseCase registerDeviceUseCase;
	private final UpdateDeviceUseCase updateDeviceUseCase;
	private final DeleteDeviceUseCase deleteDeviceUseCase;
	private final ListDevicesUseCase listDevicesUseCase;
	private final DeviceWebMapper deviceWebMapper;
	private final CommonTextMapper commonTextMapper;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public DeviceResponse register(@Valid @RequestBody final RegisterDeviceRequest request) {
		final UUID id = UUID.fromString(request.id());
		final DeviceType type = DeviceType.valueOf(request.type());
		final UUID zoneId = request.zoneId() != null ? UUID.fromString(request.zoneId()) : null;
		final String label = this.commonTextMapper.normalizeFreeText(request.label());
		final Device device = this.registerDeviceUseCase.register(id, type, label, zoneId);
		return this.deviceWebMapper.toResponse(device);
	}

	@GetMapping
	public List<DeviceResponse> listAll() {
		return this.listDevicesUseCase.listAll().stream()
			.map(this.deviceWebMapper::toResponse)
			.toList();
	}

	@PutMapping("/{id}")
	public DeviceResponse update(@PathVariable final UUID id, @Valid @RequestBody final UpdateDeviceRequest request) {
		final UUID zoneId = request.zoneId() != null ? UUID.fromString(request.zoneId()) : null;
		final String label = this.commonTextMapper.normalizeFreeText(request.label());
		final Device device = this.updateDeviceUseCase.update(id, label, zoneId, request.enabled());
		return this.deviceWebMapper.toResponse(device);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable final UUID id) {
		this.deleteDeviceUseCase.delete(id);
	}
}
