package fr.cerbere.component.cerbere_history.domain.port.out.device;

import fr.cerbere.component.cerbere_history.domain.model.DeviceEventRecord;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;

import java.time.Instant;
import java.util.UUID;

/**
 * Port de sortie : persistance de l'historique des événements de device.
 */
public interface DeviceEventRecordRepository {

	DeviceEventRecord save(DeviceEventRecord event);

	PageResult<DeviceEventRecord> search(UUID deviceId, UUID zoneId, String eventType, Instant from, Instant to, PageRequest pageRequest);
}
