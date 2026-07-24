package fr.cerbere.component.cerbere_history.application.usecase.device;

import fr.cerbere.component.cerbere_history.domain.model.DeviceEventRecord;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;
import fr.cerbere.component.cerbere_history.domain.port.in.device.ListDeviceEventsUseCase;
import fr.cerbere.component.cerbere_history.domain.port.in.device.RecordDeviceEventUseCase;
import fr.cerbere.component.cerbere_history.domain.port.out.device.DeviceEventRecordRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Implémentation des use-cases d'historisation/consultation des événements
 * de device, regroupés car ils partagent le même port de sortie.
 */
@RequiredArgsConstructor
public final class DeviceEventHistoryService implements RecordDeviceEventUseCase, ListDeviceEventsUseCase {

	private final DeviceEventRecordRepository deviceEventRecordRepository;

	@Override
	public void record(final DeviceEventRecord event) {
		this.deviceEventRecordRepository.save(event);
	}

	@Override
	public PageResult<DeviceEventRecord> list(final UUID deviceId, final UUID zoneId, final String eventType,
											   final Instant from, final Instant to, final PageRequest pageRequest) {
		return this.deviceEventRecordRepository.search(deviceId, zoneId, eventType, from, to, pageRequest);
	}
}
