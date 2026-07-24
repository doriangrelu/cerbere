package fr.cerbere.component.cerbere_history.adapter.in.web.device;

import fr.cerbere.component.cerbere_history.adapter.in.web.device.dto.DeviceEventHistoryWebMapper;
import fr.cerbere.component.cerbere_history.domain.model.DeviceEventRecord;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;
import fr.cerbere.component.cerbere_history.domain.port.in.device.ListDeviceEventsUseCase;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.shared.dto.history.DeviceEventHistoryResponse;
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
 * API REST de consultation de l'historique des événements de device. Aucune
 * mutation ici : {@code cerbere-history} n'est alimenté que par Kafka.
 */
@RestController
@RequestMapping("/api/history/device-events")
@RequiredArgsConstructor
public final class DeviceEventHistoryController {

	private final ListDeviceEventsUseCase listDeviceEventsUseCase;
	private final DeviceEventHistoryWebMapper deviceEventHistoryWebMapper;

	@GetMapping
	public Page<DeviceEventHistoryResponse> list(@RequestParam(required = false) final UUID deviceId,
												  @RequestParam(required = false) final UUID zoneId,
												  @RequestParam(required = false) final String eventType,
												  @RequestParam(required = false) final Instant from,
												  @RequestParam(required = false) final Instant to,
												  final Pageable pageable) {
		final PageResult<DeviceEventRecord> result = this.listDeviceEventsUseCase.list(
			deviceId, zoneId, eventType, from, to,
			new PageRequest(pageable.getPageNumber(), pageable.getPageSize())
		);
		final List<DeviceEventHistoryResponse> content = result.content().stream()
			.map(this.deviceEventHistoryWebMapper::toResponse)
			.toList();
		return new PageImpl<>(content, pageable, result.totalElements());
	}
}
