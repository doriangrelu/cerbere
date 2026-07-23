package fr.cerbere.component.cerbere_bff.adapter.in.web.zone;

/**
 * Modèle de présentation d'une ligne du tableau Zones : agrège {@code ZoneResponse}
 * avec le nombre de devices rattachés, dérivé côté BFF de {@code DeviceResponse.zoneId()}
 * ({@code ZoneResponse} ne porte plus cette information — voir ADR 0017).
 */
public record ZoneRow(String id, String name, boolean violation, long deviceCount) {
}
