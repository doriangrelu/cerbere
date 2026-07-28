package fr.cerbere.shared.dto.devicemock;

/**
 * Contrat REST partagé entre {@code cerbere-devices-mock} (producteur) et
 * {@code cerbere-bff} (consommateur, section "mode test") — voir ADR 0010/0013.
 * {@code friendlyName} est l'identifiant MQTT publié par le device (voir ADR 0021,
 * le Mock se comporte comme un vrai device Zigbee2MQTT). {@code online} indique
 * s'il est joignable sur le réseau : un device hors réseau n'émet plus rien
 * (voir ADR 0024), de quoi éprouver la supervision de vie de {@code cerbere-core}.
 */
public record SimulatedDeviceResponse(
	String id,
	String type,
	String label,
	String friendlyName,
	boolean online,
	String currentState
) {
}
