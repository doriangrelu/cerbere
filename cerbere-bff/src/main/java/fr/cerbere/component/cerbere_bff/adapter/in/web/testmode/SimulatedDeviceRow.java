package fr.cerbere.component.cerbere_bff.adapter.in.web.testmode;

/**
 * Modèle de présentation d'une ligne du tableau Mode test : agrège
 * {@code SimulatedDeviceResponse} avec le nom de la zone résolu côté BFF (le
 * template ne doit jamais afficher un UUID brut à l'usager).
 */
public record SimulatedDeviceRow(String id, String type, String label, String zoneName, boolean autoSimulate, String currentState) {
}
