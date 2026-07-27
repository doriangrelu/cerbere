package fr.cerbere.shared.dto.devicemock;

/**
 * Contrat REST partagé entre {@code cerbere-devices-mock} (producteur) et
 * {@code cerbere-bff} (consommateur, section "mode test") — voir ADR 0010/0013.
 * {@code friendlyName} est l'identifiant MQTT publié par le device (voir ADR 0021,
 * le Mock se comporte comme un vrai device Zigbee2MQTT) : "lié" si sa valeur
 * correspond à un device connu de {@code cerbere-core}, "orphelin" sinon.
 */
public record SimulatedDeviceResponse(
	String id,
	String type,
	String label,
	String friendlyName,
	boolean autoSimulate,
	String currentState
) {
}
