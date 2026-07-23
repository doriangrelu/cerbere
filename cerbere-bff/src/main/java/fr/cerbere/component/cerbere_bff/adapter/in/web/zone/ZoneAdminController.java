package fr.cerbere.component.cerbere_bff.adapter.in.web.zone;

import fr.cerbere.component.cerbere_bff.adapter.support.ProblemDetailMessages;
import fr.cerbere.component.cerbere_bff.client.device.DeviceCoreClient;
import fr.cerbere.component.cerbere_bff.client.zone.ZoneCoreClient;
import fr.cerbere.shared.dto.device.DeviceResponse;
import fr.cerbere.shared.dto.zone.RegisterZoneRequest;
import fr.cerbere.shared.dto.zone.UpdateZoneRequest;
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
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Administration des zones (création, renommage, suppression). La suppression
 * d'une zone contenant encore des devices est refusée par {@code cerbere-core}
 * (409 Conflict) — l'erreur est affichée dans le fragment de table plutôt que
 * de faire planter l'écran. Les actions htmx retournent uniquement ce fragment
 * — voir docs/best-practices/frontend-conventions.md. {@code ZoneResponse} ne
 * portant plus la liste des devices rattachés (voir ADR 0017), le nombre de
 * devices par zone est dérivé ici de {@code DeviceResponse.zoneId()}.
 */
@Controller
@RequiredArgsConstructor
public final class ZoneAdminController {

	private static final String ZONES_ATTRIBUTE = "zones";
	private static final String ZONE_ERROR_ATTRIBUTE = "zoneError";
	private static final String ZONE_TABLE_FRAGMENT = "fragments/zone-table :: zoneTable";

	private final ZoneCoreClient zoneCoreClient;
	private final DeviceCoreClient deviceCoreClient;
	private final ProblemDetailMessages problemDetailMessages;

	@GetMapping("/zones")
	public String list(final Model model) {
		model.addAttribute(ZONES_ATTRIBUTE, this.zoneRows());
		return "zone/list";
	}

	@PostMapping("/zones")
	public String register(@RequestParam final String name, final Model model) {
		this.zoneCoreClient.register(new RegisterZoneRequest(name));
		model.addAttribute(ZONES_ATTRIBUTE, this.zoneRows());
		return ZONE_TABLE_FRAGMENT;
	}

	@PutMapping("/zones/{id}")
	public String rename(@PathVariable final String id, @RequestParam final String name, final Model model) {
		this.zoneCoreClient.update(id, new UpdateZoneRequest(name));
		model.addAttribute(ZONES_ATTRIBUTE, this.zoneRows());
		return ZONE_TABLE_FRAGMENT;
	}

	@DeleteMapping("/zones/{id}")
	public String delete(@PathVariable final String id, final Model model) {
		try {
			this.zoneCoreClient.delete(id);
		} catch (final HttpClientErrorException.Conflict exception) {
			model.addAttribute(ZONE_ERROR_ATTRIBUTE, this.problemDetailMessages.extractDetail(exception));
		}
		model.addAttribute(ZONES_ATTRIBUTE, this.zoneRows());
		return ZONE_TABLE_FRAGMENT;
	}

	private List<ZoneRow> zoneRows() {
		final List<ZoneResponse> zones = this.zoneCoreClient.listAll();
		final List<DeviceResponse> devices = this.deviceCoreClient.listAll();
		final Map<String, Long> deviceCountByZoneId = devices.stream()
			.filter(device -> device.zoneId() != null)
			.collect(Collectors.groupingBy(DeviceResponse::zoneId, Collectors.counting()));
		return zones.stream()
			.map(zone -> new ZoneRow(zone.id(), zone.name(), zone.violation(), deviceCountByZoneId.getOrDefault(zone.id(), 0L)))
			.toList();
	}
}
