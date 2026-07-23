package fr.cerbere.component.cerbere_bff.adapter.in.web.device;

import fr.cerbere.component.cerbere_bff.client.device.DeviceCoreClient;
import fr.cerbere.component.cerbere_bff.client.zone.ZoneCoreClient;
import fr.cerbere.shared.dto.device.DeviceResponse;
import fr.cerbere.shared.dto.device.RegisterDeviceRequest;
import fr.cerbere.shared.dto.device.UpdateDeviceRequest;
import fr.cerbere.shared.dto.zone.ZoneResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Administration du registre officiel des devices (création, activation/désactivation,
 * suppression). L'id est généré ici (côté BFF) à la création : l'usager n'a jamais à
 * saisir ni à voir d'UUID brut. Le device créé est propagé à {@code cerbere-devices-mock}
 * via l'événement Kafka {@code device.created} publié par {@code cerbere-core} — voir ADR 0016.
 * Les actions htmx retournent uniquement le fragment concerné (swap), jamais la page entière.
 */
@Controller
@RequiredArgsConstructor
public final class DeviceAdminController {

	private static final String DEVICES_ATTRIBUTE = "devices";
	private static final String ZONES_ATTRIBUTE = "zones";
	private static final String DEVICE_ERROR_ATTRIBUTE = "deviceError";
	private static final String DEVICE_SECTION_FRAGMENT = "fragments/device-table :: deviceSection";

	private final DeviceCoreClient deviceCoreClient;
	private final ZoneCoreClient zoneCoreClient;

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
		final List<DeviceRow> deviceRows = devices.stream()
			.map(device -> this.toRow(device, zoneNamesById))
			.toList();
		model.addAttribute(DEVICES_ATTRIBUTE, deviceRows);
		model.addAttribute(ZONES_ATTRIBUTE, zones);
	}

	private DeviceRow toRow(final DeviceResponse device, final Map<String, String> zoneNamesById) {
		final String zoneName = device.zoneId() == null ? null : zoneNamesById.getOrDefault(device.zoneId(), "Zone supprimée");
		return new DeviceRow(device.id(), device.type(), device.label(), device.zoneId(), zoneName, device.enabled());
	}

	private String blankToNull(final String value) {
		return (value == null || value.isBlank()) ? null : value;
	}
}
