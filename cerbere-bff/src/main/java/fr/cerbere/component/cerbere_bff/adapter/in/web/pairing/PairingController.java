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
 * disponible, contrairement au Mode test réservé au pilotage du Mock.
 * <p>
 * Chaque device détecté ne se voit proposer que des cibles de son propre type :
 * appairer un détecteur de mouvement à une entrée déclarée comme contact ferait
 * interpréter ses payloads selon le mauvais contrat. Le couple soumis est
 * revalidé ici avant transmission (voir docs/best-practices/frontend-conventions.md),
 * et le Bridge refuse de toute façon un appairage incompatible.
 */
@Controller
@RequiredArgsConstructor
public class PairingController {

	private static final String DISCOVERED_DEVICES_ATTRIBUTE = "discoveredDevices";
	private static final String HAS_PAIRABLE_DEVICES_ATTRIBUTE = "hasPairableDevices";
	private static final String PAIRING_ERROR_ATTRIBUTE = "pairingError";
	private static final String PAIRING_SECTION_FRAGMENT = "fragments/pairing-table :: pairingSection";
	private static final String UNKNOWN_TYPE = "—";
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
		final List<DiscoveredDeviceResponse> discoveredDevices = this.deviceBridgeClient.listDiscoveredDevices();
		final List<DeviceResponse> pairableDevices = this.pairableDevices();

		if (!this.isPairable(friendlyName, coreDeviceId, discoveredDevices, pairableDevices)) {
			model.addAttribute(PAIRING_ERROR_ATTRIBUTE, "Device sélectionné introuvable, déjà apparié, ou de type incompatible.");
			this.populateModel(model, discoveredDevices, pairableDevices);
			return PAIRING_SECTION_FRAGMENT;
		}
		try {
			this.deviceBridgeClient.pair(friendlyName, coreDeviceId);
		} catch (final HttpClientErrorException exception) {
			model.addAttribute(PAIRING_ERROR_ATTRIBUTE, this.problemDetailMessages.extractDetail(exception));
			this.populateModel(model, discoveredDevices, pairableDevices);
			return PAIRING_SECTION_FRAGMENT;
		}
		// L'appairage vient de changer l'état côté Bridge/Core (friendlyName renommé,
		// device désormais lié) : le rendu doit repartir d'une lecture fraîche,
		// jamais du snapshot pris avant l'appel.
		this.populateModel(model);
		return PAIRING_SECTION_FRAGMENT;
	}

	/**
	 * Revérifie que la cible existe, n'est pas déjà appairée, et que son type
	 * correspond bien à celui détecté sur le device physique.
	 */
	private boolean isPairable(final String friendlyName, final String coreDeviceId,
							   final List<DiscoveredDeviceResponse> discoveredDevices,
							   final List<DeviceResponse> pairableDevices) {
		final DiscoveredDeviceResponse discovered = discoveredDevices.stream()
			.filter(device -> device.friendlyName().equals(friendlyName))
			.findFirst()
			.orElse(null);
		if (discovered == null) {
			return false;
		}
		return pairableDevices.stream()
			.filter(device -> device.id().equals(coreDeviceId))
			.anyMatch(device -> this.typesMatch(discovered, device));
	}

	private void populateModel(final Model model) {
		this.populateModel(model, this.deviceBridgeClient.listDiscoveredDevices(), this.pairableDevices());
	}

	private void populateModel(final Model model, final List<DiscoveredDeviceResponse> discoveredDevices,
							   final List<DeviceResponse> pairableDevices) {
		final List<DiscoveredDeviceRow> rows = discoveredDevices.stream()
			.map(device -> this.toRow(device, pairableDevices))
			.toList();
		model.addAttribute(DISCOVERED_DEVICES_ATTRIBUTE, rows);
		model.addAttribute(HAS_PAIRABLE_DEVICES_ATTRIBUTE, !pairableDevices.isEmpty());
	}

	private List<DeviceResponse> pairableDevices() {
		return this.deviceCoreClient.listAll().stream()
			.filter(device -> !device.linked())
			.toList();
	}

	private DiscoveredDeviceRow toRow(final DiscoveredDeviceResponse device, final List<DeviceResponse> pairableDevices) {
		final List<DeviceResponse> candidates = pairableDevices.stream()
			.filter(candidate -> this.typesMatch(device, candidate))
			.toList();
		final String inferredType = device.inferredType() == null ? UNKNOWN_TYPE : device.inferredType();
		return new DiscoveredDeviceRow(device.friendlyName(), inferredType, this.format(device.lastSeenAt()), candidates);
	}

	/**
	 * Un type indéterminable (payload non reconnu par le bridge) n'exclut rien :
	 * il n'y a alors rien à comparer, le contrôle serait arbitraire.
	 */
	private boolean typesMatch(final DiscoveredDeviceResponse discovered, final DeviceResponse coreDevice) {
		return discovered.inferredType() == null || discovered.inferredType().equals(coreDevice.type());
	}

	private String format(final Instant instant) {
		return instant == null ? UNKNOWN_TYPE : LAST_SEEN_AT_FORMATTER.format(instant);
	}
}
