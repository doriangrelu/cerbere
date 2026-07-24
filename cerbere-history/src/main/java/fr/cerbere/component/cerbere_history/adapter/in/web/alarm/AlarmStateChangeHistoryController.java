package fr.cerbere.component.cerbere_history.adapter.in.web.alarm;

import fr.cerbere.component.cerbere_history.adapter.in.web.alarm.dto.AlarmStateChangeHistoryWebMapper;
import fr.cerbere.component.cerbere_history.domain.model.AlarmStateChangeRecord;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;
import fr.cerbere.component.cerbere_history.domain.port.in.alarm.ListAlarmStateChangesUseCase;
import fr.cerbere.shared.dto.history.AlarmStateChangeHistoryResponse;
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

/**
 * API REST de consultation de l'historique des changements d'état de l'alarme.
 */
@RestController
@RequestMapping("/api/history/alarm-state-changes")
@RequiredArgsConstructor
public final class AlarmStateChangeHistoryController {

	private final ListAlarmStateChangesUseCase listAlarmStateChangesUseCase;
	private final AlarmStateChangeHistoryWebMapper alarmStateChangeHistoryWebMapper;

	@GetMapping
	public Page<AlarmStateChangeHistoryResponse> list(@RequestParam(required = false) final Instant from,
													   @RequestParam(required = false) final Instant to,
													   final Pageable pageable) {
		final PageResult<AlarmStateChangeRecord> result = this.listAlarmStateChangesUseCase.list(
			from, to, new PageRequest(pageable.getPageNumber(), pageable.getPageSize())
		);
		final List<AlarmStateChangeHistoryResponse> content = result.content().stream()
			.map(this.alarmStateChangeHistoryWebMapper::toResponse)
			.toList();
		return new PageImpl<>(content, pageable, result.totalElements());
	}
}
