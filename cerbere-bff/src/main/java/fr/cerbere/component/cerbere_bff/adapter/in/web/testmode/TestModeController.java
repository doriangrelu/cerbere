package fr.cerbere.component.cerbere_bff.adapter.in.web.testmode;

import fr.cerbere.component.cerbere_bff.client.devicemock.DeviceMockClient;
import fr.cerbere.component.cerbere_bff.client.zone.ZoneCoreClient;
import fr.cerbere.shared.dto.devicemock.RegisterSimulatedDeviceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Section "mode test" : pilotage de {@code cerbere-devices-mock} (création de
 * devices simulés, déclenchement manuel d'événements). N'existe que si
 * {@code cerbere.bff.test-mode.enabled=true} — en usage réel (bridge de
 * devices physiques à la place du mock), cette section n'a pas de sens et ne
 * doit pas être exposée.
 */
@Controller
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "cerbere.bff.test-mode", name = "enabled", havingValue = "true")
public final class TestModeController {

	private static final String SIMULATED_DEVICES_ATTRIBUTE = "simulatedDevices";
	private static final String ZONES_ATTRIBUTE = "zones";
	private static final String SIMULATED_DEVICE_TABLE_FRAGMENT = "fragments/simulated-device-table :: simulatedDeviceTable";

	private final DeviceMockClient deviceMockClient;
	private final ZoneCoreClient zoneCoreClient;

	@GetMapping("/test-mode")
	public String home(final Model model) {
		model.addAttribute(SIMULATED_DEVICES_ATTRIBUTE, this.deviceMockClient.listAll());
		model.addAttribute(ZONES_ATTRIBUTE, this.zoneCoreClient.listAll());
		return "testmode/dashboard";
	}

	@PostMapping("/test-mode/devices")
	public String register(@RequestParam final String type,
							@RequestParam final String label,
							@RequestParam(required = false) final String zoneId,
							@RequestParam(required = false, defaultValue = "false") final boolean autoSimulate,
							final Model model) {
		this.deviceMockClient.register(new RegisterSimulatedDeviceRequest(type, label, this.blankToNull(zoneId), autoSimulate));
		model.addAttribute(SIMULATED_DEVICES_ATTRIBUTE, this.deviceMockClient.listAll());
		return SIMULATED_DEVICE_TABLE_FRAGMENT;
	}

	@PostMapping("/test-mode/devices/{id}/events")
	public String triggerEvent(@PathVariable final String id, @RequestParam final String state, final Model model) {
		this.deviceMockClient.triggerEvent(id, state);
		model.addAttribute(SIMULATED_DEVICES_ATTRIBUTE, this.deviceMockClient.listAll());
		return SIMULATED_DEVICE_TABLE_FRAGMENT;
	}

	private String blankToNull(final String value) {
		return (value == null || value.isBlank()) ? null : value;
	}
}
