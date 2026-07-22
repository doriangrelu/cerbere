package fr.cerbere.component.cerbere_bff.adapter.in.web.device;

import fr.cerbere.component.cerbere_bff.client.device.DeviceCoreClient;
import fr.cerbere.component.cerbere_bff.client.devicemock.DeviceMockClient;
import fr.cerbere.component.cerbere_bff.client.zone.ZoneCoreClient;
import fr.cerbere.shared.dto.device.DeviceResponse;
import fr.cerbere.shared.dto.device.RegisterDeviceRequest;
import fr.cerbere.shared.dto.device.UpdateDeviceRequest;
import fr.cerbere.shared.dto.devicemock.SimulatedDeviceResponse;
import fr.cerbere.shared.dto.zone.ZoneResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Administration du registre officiel des devices (création, activation/désactivation,
 * suppression). L'usager ne saisit ni ne voit jamais d'UUID brut : la sélection du
 * device à enregistrer se fait par libellé (liste déroulante des devices simulés pas
 * encore enregistrés, agrégée ici depuis {@code cerbere-devices-mock} et
 * {@code cerbere-core}), et les identifiants soumis sont revalidés côté BFF avant
 * d'être transmis à {@code cerbere-core} — voir docs/best-practices/frontend-conventions.md.
 * Les actions htmx retournent uniquement le fragment concerné (swap), jamais la page entière.
 */
@Controller
@RequiredArgsConstructor
public final class DeviceAdminController {

    private static final String DEVICES_ATTRIBUTE = "devices";
    private static final String ZONES_ATTRIBUTE = "zones";
    private static final String AVAILABLE_DEVICES_ATTRIBUTE = "availableDevices";
    private static final String DEVICE_ERROR_ATTRIBUTE = "deviceError";
    private static final String DEVICE_SECTION_FRAGMENT = "fragments/device-table :: deviceSection";

    private final DeviceCoreClient deviceCoreClient;
    private final ZoneCoreClient zoneCoreClient;
    private final DeviceMockClient deviceMockClient;

    @GetMapping("/devices")
    public String list(final Model model) {
        this.populateModel(model);
        return "device/list";
    }

    @PostMapping("/devices")
    public String register(@RequestParam final String type,
                           @RequestParam(required = false) final String label,
                           @RequestParam(required = false) final String zoneId,
                           final Model model) {
        if (type == null) {
            model.addAttribute(DEVICE_ERROR_ATTRIBUTE, "Merci de séléctionner un type de device");
            this.populateModel(model);
            return DEVICE_SECTION_FRAGMENT;
        }
        if (zoneId != null && !zoneId.isBlank() && this.zoneCoreClient.listAll().stream().noneMatch(zone -> zone.id().equals(zoneId))) {
            model.addAttribute(DEVICE_ERROR_ATTRIBUTE, "Zone sélectionnée introuvable.");
            this.populateModel(model);
            return DEVICE_SECTION_FRAGMENT;
        }
        this.deviceCoreClient.register(new RegisterDeviceRequest(UUID.randomUUID().toString(), type, label, this.blankToNull(zoneId)));
        this.populateModel(model);
        return DEVICE_SECTION_FRAGMENT;
    }

    @PutMapping("/devices/{id}/toggle")
    public String toggle(@PathVariable final String id,
                         @RequestParam final String label,
                         @RequestParam(required = false) final String zoneId,
                         @RequestParam final boolean enabled,
                         final Model model) {
        this.deviceCoreClient.update(id, new UpdateDeviceRequest(label, this.blankToNull(zoneId), !enabled));
        this.populateModel(model);
        return DEVICE_SECTION_FRAGMENT;
    }

    @DeleteMapping("/devices/{id}")
    public String delete(@PathVariable final String id, final Model model) {
        this.deviceCoreClient.delete(id);
        this.populateModel(model);
        return DEVICE_SECTION_FRAGMENT;
    }

    private void populateModel(final Model model) {
        final List<DeviceResponse> devices = this.deviceCoreClient.listAll();
        final List<ZoneResponse> zones = this.zoneCoreClient.listAll();
        final Map<String, String> zoneNamesById = zones.stream()
                .collect(Collectors.toMap(ZoneResponse::id, ZoneResponse::name));
        final Set<String> registeredIds = devices.stream().map(DeviceResponse::id).collect(Collectors.toSet());
        final List<DeviceRow> deviceRows = devices.stream()
                .map(device -> this.toRow(device, zoneNamesById))
                .toList();
        final List<SimulatedDeviceResponse> availableDevices = this.deviceMockClient.listAll().stream()
                .filter(simulated -> !registeredIds.contains(simulated.id()))
                .toList();
        model.addAttribute(DEVICES_ATTRIBUTE, deviceRows);
        model.addAttribute(ZONES_ATTRIBUTE, zones);
        model.addAttribute(AVAILABLE_DEVICES_ATTRIBUTE, availableDevices);
    }

    private DeviceRow toRow(final DeviceResponse device, final Map<String, String> zoneNamesById) {
        final String zoneName = device.zoneId() == null ? null : zoneNamesById.getOrDefault(device.zoneId(), "Zone supprimée");
        return new DeviceRow(device.id(), device.type(), device.label(), device.zoneId(), zoneName, device.enabled());
    }

    private String blankToNull(final String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
