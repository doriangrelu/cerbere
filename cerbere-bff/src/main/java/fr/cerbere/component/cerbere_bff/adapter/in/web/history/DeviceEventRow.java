package fr.cerbere.component.cerbere_bff.adapter.in.web.history;

import java.util.Map;

/**
 * Modèle de présentation d'une ligne de l'historique des événements de
 * device : {@code deviceId}/{@code zoneId} résolus en libellé/nom côté BFF
 * (le template ne doit jamais afficher un UUID brut à l'usager).
 */
public record DeviceEventRow(String eventId, String deviceLabel, String zoneName, String eventType,
							  Map<String, Object> payload, String occurredAt) {
}
