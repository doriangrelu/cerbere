package fr.cerbere.component.cerbere_bff.client.history;

import fr.cerbere.shared.dto.history.AlarmStateChangeHistoryResponse;
import fr.cerbere.shared.dto.history.AlertHistoryResponse;
import fr.cerbere.shared.dto.history.DeviceEventHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Client REST vers les endpoints {@code /api/history/*} de {@code cerbere-history}.
 * DTOs partagés via {@code cerbere-shared-kernel} (voir ADR 0010/0013) : pas de
 * copie locale du contrat REST. Lecture seule : {@code cerbere-history} n'expose
 * aucune mutation, il n'est alimenté que par Kafka.
 */
@Component
@RequiredArgsConstructor
public final class HistoryCoreClient {

	private final RestClient historyRestClient;

	public HistoryPage<DeviceEventHistoryResponse> listDeviceEvents(final int page, final int size) {
		return this.historyRestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/api/history/device-events")
				.queryParam("page", page)
				.queryParam("size", size)
				.build())
			.retrieve()
			.body(new ParameterizedTypeReference<HistoryPage<DeviceEventHistoryResponse>>() {
			});
	}

	public HistoryPage<AlarmStateChangeHistoryResponse> listAlarmStateChanges(final int page, final int size) {
		return this.historyRestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/api/history/alarm-state-changes")
				.queryParam("page", page)
				.queryParam("size", size)
				.build())
			.retrieve()
			.body(new ParameterizedTypeReference<HistoryPage<AlarmStateChangeHistoryResponse>>() {
			});
	}

	public HistoryPage<AlertHistoryResponse> listAlerts(final int page, final int size) {
		return this.historyRestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/api/history/alerts")
				.queryParam("page", page)
				.queryParam("size", size)
				.build())
			.retrieve()
			.body(new ParameterizedTypeReference<HistoryPage<AlertHistoryResponse>>() {
			});
	}
}
