package fr.cerbere.component.cerbere_bff.adapter.in.web.testmode;

/**
 * Modèle de présentation d'une ligne du tableau Mode test : agrège
 * {@code SimulatedDeviceResponse} avec le nom de la zone résolu côté BFF (le
 * template ne doit jamais afficher un UUID brut à l'usager). {@code bound}
 * indique si ce device simulé correspond déjà à un device du registre officiel
 * (id partagé) — voir ADR 0020. Un device orphelin ({@code !bound}) peut être
 * lié à un device officiel non encore lié depuis ce même écran.
 */
public record SimulatedDeviceRow(String id, String type, String label, String zoneName, boolean autoSimulate, String currentState, boolean bound) {
}
