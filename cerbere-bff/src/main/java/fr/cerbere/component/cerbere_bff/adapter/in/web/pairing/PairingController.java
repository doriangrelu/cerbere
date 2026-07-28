package fr.cerbere.component.cerbere_bff.adapter.in.web.pairing;

import fr.cerbere.component.cerbere_bff.adapter.support.ProblemDetailMessages;
import fr.cerbere.component.cerbere_bff.client.device.DeviceCoreClient;
import fr.cerbere.component.cerbere_bff.client.devicebridge.DeviceBridgeClient;
import fr.cerbere.shared.dto.device.DeviceResponse;
import fr.cerbere.shared.dto.devicebridge.DiscoveredDeviceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Écran Appairage : rattache un device physique (ou simulé) vu sur MQTT à un
 * device du registre officiel. Passe exclusivement par
 * {@code cerbere-devices-bridge} (voir ADR 0023), qui demande à la passerelle
 * Zigbee2MQTT de renommer le {@code friendly_name} — le même geste, et le même
 * code, que le matériel soit réel ou simulé. Cet écran est donc toujours
 * disponible, contrairement au Mode test réservé au pilotage du Mock. L'id du
 * device officiel soumis est revalidé ici avant transmission au Bridge (voir
 * docs/best-practices/frontend-conventions.md).
 */
@Controller
@RequiredArgsConstructor
public final class PairingController {

	private static final String DISCOVERED_DEVICES_ATTRIBUTE = "discoveredDevices";
	private static final String PAIRABLE_DEVICES_ATTRIBUTE = "pairableDevices";
	private static final String PAIRING_ERROR_ATTRIBUTE = "pairingError";
	private static final String PAIRING_SECTION_FRAGMENT = "fragments/pairing-table :: pairingSection";
	private static final DateTimeFormatter LAST_SEEN_AT_FORMATTER =
		DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

	private final DeviceBridgeClient deviceBridgeClient;
	private final DeviceCoreClient deviceCoreClient;
	private final ProblemDetailMessages problemDetailMessages;

	@GetMapping("/pairing")
	public String list(final Model model) {
		this.populateModel(model);
		return "pairing/list";
	}

	@PostMapping("/pairing")
	public String pair(@RequestParam final String friendlyName,
						@RequestParam final String coreDeviceId,
						final Model model) {
		final boolean targetExistsAndUnlinked = this.deviceCoreClient.listAll().stream()
			.anyMatch(device -> device.id().equals(coreDeviceId) && !device.linked());
		if (!targetExistsAndUnlinked) {
			model.addAttribute(PAIRING_ERROR_ATTRIBUTE, "Device sélectionné introuvable ou déjà apparié.");
			this.populateModel(model);
			return PAIRING_SECTION_FRAGMENT;
		}
		try {
			this.deviceBridgeClient.pair(friendlyName, coreDeviceId);
		} catch (final HttpClientErrorException exception) {
			model.addAttribute(PAIRING_ERROR_ATTRIBUTE, this.problemDetailMessages.extractDetail(exception));
		}
		this.populateModel(model);
		return PAIRING_SECTION_FRAGMENT;
	}

	private void populateModel(final Model model) {
		final List<DiscoveredDeviceRow> rows = this.deviceBridgeClient.listDiscoveredDevices().stream()
			.map(this::toRow)
			.toList();
		final List<DeviceResponse> pairableDevices = this.deviceCoreClient.listAll().stream()
			.filter(device -> !device.linked())
			.toList();
		model.addAttribute(DISCOVERED_DEVICES_ATTRIBUTE, rows);
		model.addAttribute(PAIRABLE_DEVICES_ATTRIBUTE, pairableDevices);
	}

	private DiscoveredDeviceRow toRow(final DiscoveredDeviceResponse device) {
		final String inferredType = device.inferredType() == null ? "—" : device.inferredType();
		return new DiscoveredDeviceRow(device.friendlyName(), inferredType, this.format(device.lastSeenAt()));
	}

	private String format(final Instant instant) {
		return instant == null ? "—" : LAST_SEEN_AT_FORMATTER.format(instant);
	}
}
