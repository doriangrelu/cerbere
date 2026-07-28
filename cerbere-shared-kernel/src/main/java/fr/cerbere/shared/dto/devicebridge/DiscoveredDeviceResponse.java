package fr.cerbere.shared.dto.devicebridge;

import java.time.Instant;

/**
 * Contrat REST partagé entre {@code cerbere-devices-bridge} (producteur) et
 * {@code cerbere-bff} (consommateur, écran Appairage) — voir ADR 0010/0013.
 * Représente un device vu sur MQTT dont le {@code friendlyName} ne correspond à
 * aucun device du registre officiel : matériel Zigbee fraîchement appairé (ou
 * device simulé) en attente d'être rattaché à un {@code Device} de
 * {@code cerbere-core} — voir ADR 0023. {@code inferredType} est déduit de la
 * forme du payload MQTT reçu ({@code contact}/{@code occupancy}/{@code state}),
 * pour aider l'usager à choisir la bonne cible ; {@code null} si indéterminable.
 */
public record DiscoveredDeviceResponse(
	String friendlyName,
	String inferredType,
	Instant lastSeenAt
) {
}
