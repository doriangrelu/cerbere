package fr.cerbere.component.cerbere_devices_mock.adapter.in.web;

import fr.cerbere.component.cerbere_devices_mock.adapter.in.web.dto.SimulatedDeviceWebMapper;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.ListSimulatedDevicesUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.RegisterSimulatedDeviceUseCase;
import fr.cerbere.shared.dto.devicemock.RegisterOrphanSimulatedDeviceRequest;
import fr.cerbere.shared.dto.devicemock.SimulatedDeviceResponse;
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

/**
 * API REST de gestion des devices simulés : créer un device orphelin, lister.
 * L'appairage (renommage du {@code friendlyName}) n'est délibérément pas exposé
 * ici : il passe par {@code cerbere-devices-bridge} via MQTT, seul canal
 * d'appairage, pour être identique au traitement du matériel réel — voir ADR 0023.
 */
@RestController
@RequestMapping("/api/devices-mock")
@RequiredArgsConstructor
public final class SimulatedDeviceController {

    private final ListSimulatedDevicesUseCase listSimulatedDevicesUseCase;
    private final RegisterSimulatedDeviceUseCase registerSimulatedDeviceUseCase;

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
                DeviceType.valueOf(request.type()), request.label(), false
        );
        return SimulatedDeviceWebMapper.toResponse(device);
    }

}
