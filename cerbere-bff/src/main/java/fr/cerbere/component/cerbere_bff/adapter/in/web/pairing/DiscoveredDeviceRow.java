package fr.cerbere.component.cerbere_bff.adapter.in.web.pairing;

/**
 * Modèle de présentation d'une ligne du tableau Appairage : un device vu sur
 * MQTT en attente de rattachement à un device du registre officiel (voir
 * ADR 0023). {@code friendlyName} est ici volontairement affiché — contrairement
 * à la règle générale "pas d'UUID visible" (voir frontend-conventions.md) :
 * c'est l'identité que porte physiquement le device côté Zigbee2MQTT, et le seul
 * repère dont dispose l'usager pour reconnaître le matériel qu'il vient
 * d'appairer. {@code lastSeenAt} est déjà formaté côté Java.
 */
public record DiscoveredDeviceRow(String friendlyName, String inferredType, String lastSeenAt) {
}
