package fr.cerbere.component.cerbere_bff.adapter.in.web.testmode;

import fr.cerbere.component.cerbere_bff.adapter.support.ProblemDetailMessages;
import fr.cerbere.component.cerbere_bff.client.device.DeviceCoreClient;
import fr.cerbere.component.cerbere_bff.client.devicemock.DeviceMockClient;
import fr.cerbere.shared.dto.device.DeviceResponse;
import fr.cerbere.shared.dto.devicemock.SimulatedDeviceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Section "mode test" : pilotage du faux matériel ({@code cerbere-devices-mock},
 * qui se comporte comme un vrai device Zigbee2MQTT — voir ADR 0021). Création de
 * devices simulés et déclenchement manuel d'états, toujours possibles qu'ils
 * soient appairés ou non : un device physique émet sur MQTT indépendamment de ce
 * que le registre officiel sait de lui, le faux device doit se comporter pareil.
 * L'appairage ne se fait délibérément pas ici mais sur l'écran Appairage, via
 * {@code cerbere-devices-bridge} (voir ADR 0023) — seul canal d'appairage, commun
 * au matériel réel et simulé. N'existe que si
 * {@code cerbere.bff.test-mode.enabled=true} : en usage réel, cette section n'a
 * pas de sens et ne doit pas être exposée.
 */
@Controller
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "cerbere.bff.test-mode", name = "enabled", havingValue = "true")
public final class TestModeController {

    private static final String SIMULATED_DEVICES_ATTRIBUTE = "simulatedDevices";
    private static final String SIMULATED_DEVICE_ERROR_ATTRIBUTE = "simulatedDeviceError";
    private static final String SIMULATED_DEVICE_TABLE_FRAGMENT = "fragments/simulated-device-table :: simulatedDeviceTable";

    private final DeviceMockClient deviceMockClient;
    private final DeviceCoreClient deviceCoreClient;
    private final ProblemDetailMessages problemDetailMessages;

    @GetMapping("/test-mode")
    public String home(final Model model) {
        this.populateModel(model);
        return "testmode/dashboard";
    }

    @PostMapping("/test-mode/devices/{id}/events")
    public String triggerEvent(@PathVariable final String id, @RequestParam final String state, final Model model) {
        try {
            this.deviceMockClient.triggerEvent(id, state);
        } catch (final HttpClientErrorException exception) {
            model.addAttribute(SIMULATED_DEVICE_ERROR_ATTRIBUTE, this.problemDetailMessages.extractDetail(exception));
        }
        this.populateModel(model);
        return SIMULATED_DEVICE_TABLE_FRAGMENT;
    }

    @PostMapping("/test-mode/devices")
    public String registerOrphan(@RequestParam final String type, @RequestParam final String label, final Model model) {
        try {
            this.deviceMockClient.registerOrphan(type, label);
        } catch (final HttpClientErrorException exception) {
            model.addAttribute(SIMULATED_DEVICE_ERROR_ATTRIBUTE, this.problemDetailMessages.extractDetail(exception));
        }
        this.populateModel(model);
        return SIMULATED_DEVICE_TABLE_FRAGMENT;
    }

    private void populateModel(final Model model) {
        final Map<String, DeviceResponse> coreDevicesById = this.deviceCoreClient.listAll().stream()
                .collect(Collectors.toMap(DeviceResponse::id, device -> device));
        final List<SimulatedDeviceRow> rows = this.deviceMockClient.listAll().stream()
                .map(device -> this.toRow(device, coreDevicesById))
                .toList();
        model.addAttribute(SIMULATED_DEVICES_ATTRIBUTE, rows);
    }

    private SimulatedDeviceRow toRow(final SimulatedDeviceResponse device, final Map<String, DeviceResponse> coreDevicesById) {
        final DeviceResponse pairedCoreDevice = coreDevicesById.get(device.friendlyName());
        final boolean linked = pairedCoreDevice != null && pairedCoreDevice.linked();
        return new SimulatedDeviceRow(device.id(), device.type(), device.label(), device.autoSimulate(), device.currentState(), linked);
    }
}
