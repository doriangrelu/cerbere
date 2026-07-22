package fr.cerbere.component.cerbere_bff.adapter.in.web.device;

/**
 * Modèle de présentation d'une ligne du tableau Devices : agrège
 * {@code DeviceResponse} avec le nom de la zone résolu côté BFF (le
 * template ne doit jamais afficher un UUID brut à l'usager).
 */
public record DeviceRow(String id, String type, String label, String zoneId, String zoneName, boolean enabled) {
}
