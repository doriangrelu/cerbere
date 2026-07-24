package fr.cerbere.component.cerbere_bff.adapter.in.web.history;

import fr.cerbere.component.cerbere_bff.client.device.DeviceCoreClient;
import fr.cerbere.component.cerbere_bff.client.history.HistoryCoreClient;
import fr.cerbere.component.cerbere_bff.client.history.HistoryPage;
import fr.cerbere.component.cerbere_bff.client.zone.ZoneCoreClient;
import fr.cerbere.shared.dto.device.DeviceResponse;
import fr.cerbere.shared.dto.history.AlarmStateChangeHistoryResponse;
import fr.cerbere.shared.dto.history.AlertHistoryResponse;
import fr.cerbere.shared.dto.history.DeviceEventHistoryResponse;
import fr.cerbere.shared.dto.zone.ZoneResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Consultation en lecture seule de l'historique persisté par {@code cerbere-history}
 * (événements de device, changements d'état de l'alarme, alertes). Chaque
 * section se pagine indépendamment via htmx, sans rechargement de page complète.
 */
@Controller
@RequiredArgsConstructor
public final class HistoryDashboardController {

	private static final int PAGE_SIZE = 10;
	private static final String NO_DEVICE = "—";
	private static final String NO_ZONE = "—";
	private static final String DELETED_DEVICE = "Device supprimé";
	private static final String DELETED_ZONE = "Zone supprimée";
	private static final DateTimeFormatter OCCURRED_AT_FORMATTER =
		DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

	private final HistoryCoreClient historyCoreClient;
	private final DeviceCoreClient deviceCoreClient;
	private final ZoneCoreClient zoneCoreClient;

	@GetMapping("/history")
	public String list(final Model model) {
		this.populateDeviceEvents(model, 0);
		this.populateAlarmStateChanges(model, 0);
		this.populateAlerts(model, 0);
		return "history/list";
	}

	@GetMapping("/history/device-events")
	public String deviceEvents(@RequestParam(defaultValue = "0") final int page, final Model model) {
		this.populateDeviceEvents(model, page);
		return "fragments/device-event-history-table :: table";
	}

	@GetMapping("/history/alarm-state-changes")
	public String alarmStateChanges(@RequestParam(defaultValue = "0") final int page, final Model model) {
		this.populateAlarmStateChanges(model, page);
		return "fragments/alarm-state-change-history-table :: table";
	}

	@GetMapping("/history/alerts")
	public String alerts(@RequestParam(defaultValue = "0") final int page, final Model model) {
		this.populateAlerts(model, page);
		return "fragments/alert-history-table :: table";
	}

	private void populateDeviceEvents(final Model model, final int page) {
		final HistoryPage<DeviceEventHistoryResponse> result = this.historyCoreClient.listDeviceEvents(page, PAGE_SIZE);
		final Map<String, String> deviceLabels = this.deviceLabelsById();
		final Map<String, String> zoneNames = this.zoneNamesById();
		final var rows = result.content().stream()
			.map(event -> new DeviceEventRow(
				event.eventId(),
				this.resolveDevice(event.deviceId(), deviceLabels),
				this.resolveZone(event.zoneId(), zoneNames),
				event.eventType(),
				event.payload(),
				this.format(event.occurredAt())
			))
			.toList();
		model.addAttribute("deviceEventRows", rows);
		model.addAttribute("deviceEventsPage", result.number());
		model.addAttribute("deviceEventsTotalPages", result.totalPages());
	}

	private void populateAlarmStateChanges(final Model model, final int page) {
		final HistoryPage<AlarmStateChangeHistoryResponse> result = this.historyCoreClient.listAlarmStateChanges(page, PAGE_SIZE);
		final var rows = result.content().stream()
			.map(change -> new AlarmStateChangeRow(
				change.eventId(),
				change.previousMode(),
				change.newMode(),
				change.triggered(),
				this.format(change.occurredAt())
			))
			.toList();
		model.addAttribute("alarmStateChangeRows", rows);
		model.addAttribute("alarmStateChangesPage", result.number());
		model.addAttribute("alarmStateChangesTotalPages", result.totalPages());
	}

	private void populateAlerts(final Model model, final int page) {
		final HistoryPage<AlertHistoryResponse> result = this.historyCoreClient.listAlerts(page, PAGE_SIZE);
		final Map<String, String> deviceLabels = this.deviceLabelsById();
		final Map<String, String> zoneNames = this.zoneNamesById();
		final var rows = result.content().stream()
			.map(alert -> new AlertRow(
				alert.eventId(),
				this.resolveDevice(alert.deviceId(), deviceLabels),
				this.resolveZone(alert.zoneId(), zoneNames),
				alert.severity(),
				alert.message(),
				this.format(alert.occurredAt())
			))
			.toList();
		model.addAttribute("alertRows", rows);
		model.addAttribute("alertsPage", result.number());
		model.addAttribute("alertsTotalPages", result.totalPages());
	}

	private Map<String, String> deviceLabelsById() {
		return this.deviceCoreClient.listAll().stream()
			.collect(Collectors.toMap(DeviceResponse::id, DeviceResponse::label));
	}

	private Map<String, String> zoneNamesById() {
		return this.zoneCoreClient.listAll().stream()
			.collect(Collectors.toMap(ZoneResponse::id, ZoneResponse::name));
	}

	private String resolveDevice(final String deviceId, final Map<String, String> deviceLabels) {
		return deviceId == null ? NO_DEVICE : deviceLabels.getOrDefault(deviceId, DELETED_DEVICE);
	}

	private String resolveZone(final String zoneId, final Map<String, String> zoneNames) {
		return zoneId == null ? NO_ZONE : zoneNames.getOrDefault(zoneId, DELETED_ZONE);
	}

	private String format(final Instant instant) {
		return instant == null ? "" : OCCURRED_AT_FORMATTER.format(instant);
	}
}
