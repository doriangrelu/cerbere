package fr.cerbere.component.cerbere_bff.adapter.in.web.history;

/**
 * Modèle de présentation d'une ligne de l'historique des changements d'état
 * de l'alarme. Ne porte aucun id device/zone (ce topic n'en transporte pas).
 */
public record AlarmStateChangeRow(String eventId, String previousMode, String newMode, boolean triggered, String occurredAt) {
}
