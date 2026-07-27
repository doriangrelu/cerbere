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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Section "mode test" : pilotage de {@code cerbere-devices-mock}, qui se
 * comporte comme un vrai device Zigbee2MQTT (création de devices simulés
 * orphelins, appairage à un device du registre officiel, déclenchement manuel
 * d'événements — voir ADR 0021). N'existe que si
 * {@code cerbere.bff.test-mode.enabled=true} — en usage réel (matériel Zigbee
 * physique à la place du mock), cette section n'a pas de sens et ne doit pas
 * être exposée. L'id du device officiel cible est revalidé côté BFF avant
 * transmission à {@code cerbere-devices-mock} — pas de saisie ni d'affichage
 * d'UUID brut, voir docs/best-practices/frontend-conventions.md.
 */
@Controller
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "cerbere.bff.test-mode", name = "enabled", havingValue = "true")
public final class TestModeController {

    private static final String SIMULATED_DEVICES_ATTRIBUTE = "simulatedDevices";
    private static final String UNBOUND_DEVICES_ATTRIBUTE = "unboundDevices";
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
        this.deviceMockClient.triggerEvent(id, state);
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

    @PostMapping("/test-mode/devices/{id}/rename")
    public String rename(@PathVariable final String id, @RequestParam final String coreDeviceId, final Model model) {
        final Set<String> pairedFriendlyNames = this.deviceMockClient.listAll().stream()
                .map(SimulatedDeviceResponse::friendlyName)
                .collect(Collectors.toSet());
        final boolean targetExistsAndUnpaired = this.deviceCoreClient.listAll().stream()
                .anyMatch(device -> device.id().equals(coreDeviceId) && !pairedFriendlyNames.contains(device.id()));
        if (!targetExistsAndUnpaired) {
            model.addAttribute(SIMULATED_DEVICE_ERROR_ATTRIBUTE, "Device sélectionné introuvable ou déjà apparié.");
            this.populateModel(model);
            return SIMULATED_DEVICE_TABLE_FRAGMENT;
        }
        try {
            this.deviceMockClient.rename(id, coreDeviceId);
        } catch (final HttpClientErrorException exception) {
            model.addAttribute(SIMULATED_DEVICE_ERROR_ATTRIBUTE, this.problemDetailMessages.extractDetail(exception));
        }
        this.populateModel(model);
        return SIMULATED_DEVICE_TABLE_FRAGMENT;
    }

    private void populateModel(final Model model) {
        final List<DeviceResponse> coreDevices = this.deviceCoreClient.listAll();
        final List<SimulatedDeviceResponse> mockDevices = this.deviceMockClient.listAll();
        final Set<String> coreDeviceIds = coreDevices.stream().map(DeviceResponse::id).collect(Collectors.toSet());
        final Set<String> pairedFriendlyNames = mockDevices.stream().map(SimulatedDeviceResponse::friendlyName).collect(Collectors.toSet());
        final List<SimulatedDeviceRow> rows = mockDevices.stream()
                .map(device -> this.toRow(device, coreDeviceIds.contains(device.friendlyName())))
                .toList();
        final List<DeviceResponse> unboundDevices = coreDevices.stream()
                .filter(device -> !pairedFriendlyNames.contains(device.id()))
                .toList();
        model.addAttribute(SIMULATED_DEVICES_ATTRIBUTE, rows);
        model.addAttribute(UNBOUND_DEVICES_ATTRIBUTE, unboundDevices);
    }

    private SimulatedDeviceRow toRow(final SimulatedDeviceResponse device, final boolean bound) {
        return new SimulatedDeviceRow(device.id(), device.type(), device.label(), device.autoSimulate(), device.currentState(), bound);
    }
}
