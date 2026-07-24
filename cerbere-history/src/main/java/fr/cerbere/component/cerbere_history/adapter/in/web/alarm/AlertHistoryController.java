package fr.cerbere.component.cerbere_history.adapter.in.web.alarm;

import fr.cerbere.component.cerbere_history.adapter.in.web.alarm.dto.AlertHistoryWebMapper;
import fr.cerbere.component.cerbere_history.domain.model.AlertRecord;
import fr.cerbere.component.cerbere_history.domain.model.AlertSeverity;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;
import fr.cerbere.component.cerbere_history.domain.port.in.alarm.ListAlertsUseCase;
import fr.cerbere.shared.dto.history.AlertHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * API REST de consultation de l'historique des alertes.
 */
@RestController
@RequestMapping("/api/history/alerts")
@RequiredArgsConstructor
public final class AlertHistoryController {

	private final ListAlertsUseCase listAlertsUseCase;
	private final AlertHistoryWebMapper alertHistoryWebMapper;

	@GetMapping
	public Page<AlertHistoryResponse> list(@RequestParam(required = false) final UUID deviceId,
											@RequestParam(required = false) final UUID zoneId,
											@RequestParam(required = false) final AlertSeverity severity,
											@RequestParam(required = false) final Instant from,
											@RequestParam(required = false) final Instant to,
											final Pageable pageable) {
		final PageResult<AlertRecord> result = this.listAlertsUseCase.list(
			deviceId, zoneId, severity, from, to, new PageRequest(pageable.getPageNumber(), pageable.getPageSize())
		);
		final List<AlertHistoryResponse> content = result.content().stream()
			.map(this.alertHistoryWebMapper::toResponse)
			.toList();
		return new PageImpl<>(content, pageable, result.totalElements());
	}
}
