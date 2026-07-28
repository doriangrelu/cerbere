package fr.cerbere.component.cerbere_bff.adapter.in.web.testmode;

/**
 * Modèle de présentation d'une ligne du tableau Mode test : agrège
 * {@code SimulatedDeviceResponse} (le template ne doit jamais afficher un UUID
 * brut à l'usager, {@code friendlyName} n'est donc jamais exposé une fois
 * apparié). {@code bound} reflète {@code DeviceResponse.linked()} du device
 * officiel dont le {@code friendlyName} MQTT correspond à l'id — seule source
 * de vérité tenue par {@code cerbere-core} (voir ADR 0022) : ne passe à vrai
 * qu'une fois qu'un événement a réellement été accepté, pas au simple renommage.
 * Un device orphelin ({@code !bound}) s'apparie depuis l'écran Appairage, pas
 * ici (voir ADR 0023). {@code online} reflète la joignabilité réseau simulée :
 * un device débranché n'émet plus rien (voir ADR 0024).
 */
public record SimulatedDeviceRow(String id, String type, String label, boolean online, String currentState, boolean bound) {
}
