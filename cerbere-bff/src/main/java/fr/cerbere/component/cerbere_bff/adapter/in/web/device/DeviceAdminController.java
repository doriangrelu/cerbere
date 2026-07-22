package fr.cerbere.component.cerbere_bff.adapter.in.web.device;

import fr.cerbere.component.cerbere_bff.client.device.DeviceCoreClient;
import fr.cerbere.component.cerbere_bff.client.zone.ZoneCoreClient;
import fr.cerbere.shared.dto.device.RegisterDeviceRequest;
import fr.cerbere.shared.dto.device.UpdateDeviceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Administration du registre officiel des devices (création, activation/désactivation,
 * suppression). Les actions htmx retournent uniquement le fragment de table
 * (swap), jamais la page entière — voir docs/best-practices/frontend-conventions.md.
 */
@Controller
@RequiredArgsConstructor
public final class DeviceAdminController {

	private static final String DEVICES_ATTRIBUTE = "devices";
	private static final String ZONES_ATTRIBUTE = "zones";
	private static final String DEVICE_TABLE_FRAGMENT = "fragments/device-table :: deviceTable";

	private final DeviceCoreClient deviceCoreClient;
	private final ZoneCoreClient zoneCoreClient;

	@GetMapping("/devices")
	public String list(final Model model) {
		model.addAttribute(DEVICES_ATTRIBUTE, this.deviceCoreClient.listAll());
		model.addAttribute(ZONES_ATTRIBUTE, this.zoneCoreClient.listAll());
		return "device/list";
	}

	@PostMapping("/devices")
	public String register(@RequestParam final String id,
							@RequestParam final String type,
							@RequestParam final String label,
							@RequestParam(required = false) final String zoneId,
							final Model model) {
		this.deviceCoreClient.register(new RegisterDeviceRequest(id, type, label, this.blankToNull(zoneId)));
		model.addAttribute(DEVICES_ATTRIBUTE, this.deviceCoreClient.listAll());
		return DEVICE_TABLE_FRAGMENT;
	}

	@PutMapping("/devices/{id}/toggle")
	public String toggle(@PathVariable final String id,
						  @RequestParam final String label,
						  @RequestParam(required = false) final String zoneId,
						  @RequestParam final boolean enabled,
						  final Model model) {
		this.deviceCoreClient.update(id, new UpdateDeviceRequest(label, this.blankToNull(zoneId), !enabled));
		model.addAttribute(DEVICES_ATTRIBUTE, this.deviceCoreClient.listAll());
		return DEVICE_TABLE_FRAGMENT;
	}

	@DeleteMapping("/devices/{id}")
	public String delete(@PathVariable final String id, final Model model) {
		this.deviceCoreClient.delete(id);
		model.addAttribute(DEVICES_ATTRIBUTE, this.deviceCoreClient.listAll());
		return DEVICE_TABLE_FRAGMENT;
	}

	private String blankToNull(final String value) {
		return (value == null || value.isBlank()) ? null : value;
	}
}
