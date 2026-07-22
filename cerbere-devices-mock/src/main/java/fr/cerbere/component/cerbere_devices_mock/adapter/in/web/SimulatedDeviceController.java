package fr.cerbere.component.cerbere_devices_mock.adapter.in.web;

import fr.cerbere.component.cerbere_devices_mock.adapter.in.web.dto.SimulatedDeviceWebMapper;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.ListSimulatedDevicesUseCase;
import fr.cerbere.shared.dto.devicemock.SimulatedDeviceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST de gestion des devices simulés (CRUD minimal : créer, lister).
 */
@RestController
@RequestMapping("/api/devices-mock")
@RequiredArgsConstructor
public final class SimulatedDeviceController {

    private final ListSimulatedDevicesUseCase listSimulatedDevicesUseCase;

    @GetMapping
    public List<SimulatedDeviceResponse> listAll() {
        return this.listSimulatedDevicesUseCase.listAll().stream()
                .map(SimulatedDeviceWebMapper::toResponse)
                .toList();
    }

}
