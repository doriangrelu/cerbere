package fr.cerbere.shared.dto.devicebridge;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête d'appairage d'un device découvert à un device du registre officiel
 * ({@code cerbere-core}) : {@code cerbere-devices-bridge} demande à la passerelle
 * Zigbee2MQTT (ou au Mock qui en joue le rôle) de renommer le {@code friendly_name}
 * du device en l'id du device officiel — voir ADR 0023. Contrat REST partagé
 * entre {@code cerbere-bff} (émetteur) et {@code cerbere-devices-bridge} (récepteur).
 */
public record PairDiscoveredDeviceRequest(
	@NotBlank String friendlyName,
	@NotBlank String coreDeviceId
) {
}
