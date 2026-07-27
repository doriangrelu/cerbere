package fr.cerbere.component.cerbere_devices_mock.adapter.in.web;

import fr.cerbere.component.cerbere_devices_mock.adapter.in.web.dto.SimulatedDeviceWebMapper;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.BindSimulatedDeviceUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.ListSimulatedDevicesUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.RegisterSimulatedDeviceUseCase;
import fr.cerbere.shared.dto.devicemock.BindSimulatedDeviceRequest;
import fr.cerbere.shared.dto.devicemock.RegisterOrphanSimulatedDeviceRequest;
import fr.cerbere.shared.dto.devicemock.SimulatedDeviceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * API REST de gestion des devices simulés (CRUD minimal : créer un device
 * orphelin, lister, lier à un device du registre officiel — voir ADR 0020).
 */
@RestController
@RequestMapping("/api/devices-mock")
@RequiredArgsConstructor
public final class SimulatedDeviceController {

    private final ListSimulatedDevicesUseCase listSimulatedDevicesUseCase;
    private final RegisterSimulatedDeviceUseCase registerSimulatedDeviceUseCase;
    private final BindSimulatedDeviceUseCase bindSimulatedDeviceUseCase;

    @GetMapping
    public List<SimulatedDeviceResponse> listAll() {
        return this.listSimulatedDevicesUseCase.listAll().stream()
                .map(SimulatedDeviceWebMapper::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SimulatedDeviceResponse registerOrphan(@Valid @RequestBody final RegisterOrphanSimulatedDeviceRequest request) {
        final SimulatedDevice device = this.registerSimulatedDeviceUseCase.register(
                UUID.randomUUID(), DeviceType.valueOf(request.type()), request.label(), null, false
        );
        return SimulatedDeviceWebMapper.toResponse(device);
    }

    @PostMapping("/{orphanId}/bind")
    public SimulatedDeviceResponse bind(@PathVariable final UUID orphanId, @Valid @RequestBody final BindSimulatedDeviceRequest request) {
        final UUID zoneId = request.zoneId() != null ? UUID.fromString(request.zoneId()) : null;
        final SimulatedDevice device = this.bindSimulatedDeviceUseCase.bind(
                orphanId, UUID.fromString(request.coreDeviceId()), request.label(), zoneId
        );
        return SimulatedDeviceWebMapper.toResponse(device);
    }

}
