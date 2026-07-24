package fr.cerbere.component.cerbere_bff.adapter.in.web.history;

/**
 * Modèle de présentation d'une ligne de l'historique des alertes —
 * {@code deviceId}/{@code zoneId} résolus en libellé/nom côté BFF, voir
 * {@code DeviceEventRow}.
 */
public record AlertRow(String eventId, String deviceLabel, String zoneName, String severity, String message, String occurredAt) {
}
