package fr.cerbere.component.cerbere_core.domain.port.in.alarm;

import fr.cerbere.component.cerbere_core.domain.model.DeviceEventReport;

/**
 * Port d'entrée : évaluer un événement de device rapporté (consommé depuis
 * {@code cerbere.device.events.raw}), et lever une alerte si une violation est
 * détectée pendant que le système est armé.
 */
public interface HandleDeviceEventUseCase {

	void handle(DeviceEventReport report);
}
