package fr.cerbere.component.cerbere_history.domain.port.in.device;

import fr.cerbere.component.cerbere_history.domain.model.DeviceEventRecord;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;

import java.time.Instant;
import java.util.UUID;

/**
 * Port d'entrée : consulter l'historique des événements de device, filtré
 * par device/zone/type d'événement/plage temporelle (chaque filtre est
 * optionnel — {@code null} pour ne pas filtrer sur ce critère).
 */
public interface ListDeviceEventsUseCase {

	PageResult<DeviceEventRecord> list(UUID deviceId, UUID zoneId, String eventType, Instant from, Instant to, PageRequest pageRequest);
}
