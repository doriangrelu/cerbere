package fr.cerbere.component.cerbere_bff.adapter.in.web.testmode;

/**
 * Modèle de présentation d'une ligne du tableau Mode test : agrège
 * {@code SimulatedDeviceResponse} (le template ne doit jamais afficher un UUID
 * brut à l'usager, {@code friendlyName} n'est donc jamais exposé une fois
 * apparié). {@code bound} indique si le {@code friendlyName} MQTT de ce device
 * simulé correspond déjà à un device du registre officiel — voir ADR 0021. Un
 * device orphelin ({@code !bound}) peut être apparié (renommage de son
 * {@code friendlyName}) à un device officiel non encore apparié depuis ce même écran.
 */
public record SimulatedDeviceRow(String id, String type, String label, boolean autoSimulate, String currentState, boolean bound) {
}
