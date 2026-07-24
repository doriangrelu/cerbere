package fr.cerbere.component.cerbere_history.domain.port.in.device;

import fr.cerbere.component.cerbere_history.domain.model.DeviceEventRecord;

/**
 * Port d'entrée : historiser un événement brut de device.
 */
public interface RecordDeviceEventUseCase {

	void record(DeviceEventRecord event);
}
