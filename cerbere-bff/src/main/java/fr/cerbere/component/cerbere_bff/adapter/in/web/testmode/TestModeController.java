package fr.cerbere.component.cerbere_bff.adapter.in.web.testmode;

import fr.cerbere.component.cerbere_bff.client.devicemock.DeviceMockClient;
import fr.cerbere.component.cerbere_bff.client.zone.ZoneCoreClient;
import fr.cerbere.shared.dto.devicemock.SimulatedDeviceResponse;
import fr.cerbere.shared.dto.zone.ZoneResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Section "mode test" : pilotage de {@code cerbere-devices-mock} (création de
 * devices simulés, déclenchement manuel d'événements). N'existe que si
 * {@code cerbere.bff.test-mode.enabled=true} — en usage réel (bridge de
 * devices physiques à la place du mock), cette section n'a pas de sens et ne
 * doit pas être exposée. La zone est choisie par nom (liste déroulante) et
 * revalidée côté BFF avant transmission à {@code cerbere-devices-mock} — pas
 * de saisie ni d'affichage d'UUID brut, voir docs/best-practices/frontend-conventions.md.
 */
@Controller
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "cerbere.bff.test-mode", name = "enabled", havingValue = "true")
public final class TestModeController {

    private static final String SIMULATED_DEVICES_ATTRIBUTE = "simulatedDevices";
    private static final String ZONES_ATTRIBUTE = "zones";
    private static final String SIMULATED_DEVICE_ERROR_ATTRIBUTE = "simulatedDeviceError";
    private static final String SIMULATED_DEVICE_TABLE_FRAGMENT = "fragments/simulated-device-table :: simulatedDeviceTable";

    private final DeviceMockClient deviceMockClient;
    private final ZoneCoreClient zoneCoreClient;

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

    private void populateModel(final Model model) {
        final List<ZoneResponse> zones = this.zoneCoreClient.listAll();
        final Map<String, String> zoneNamesById = zones.stream()
                .collect(Collectors.toMap(ZoneResponse::id, ZoneResponse::name));
        final List<SimulatedDeviceRow> rows = this.deviceMockClient.listAll().stream()
                .map(device -> this.toRow(device, zoneNamesById))
                .toList();
        model.addAttribute(SIMULATED_DEVICES_ATTRIBUTE, rows);
        model.addAttribute(ZONES_ATTRIBUTE, zones);
    }

    private SimulatedDeviceRow toRow(final SimulatedDeviceResponse device, final Map<String, String> zoneNamesById) {
        final String zoneName = device.zoneId() == null ? null : zoneNamesById.getOrDefault(device.zoneId(), "Zone supprimée");
        return new SimulatedDeviceRow(device.id(), device.type(), device.label(), zoneName, device.autoSimulate(), device.currentState());
    }

    private String blankToNull(final String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
