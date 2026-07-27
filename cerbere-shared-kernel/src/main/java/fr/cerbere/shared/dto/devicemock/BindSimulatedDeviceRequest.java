package fr.cerbere.shared.dto.devicemock;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête de liaison d'un device simulé orphelin à un device déjà créé dans le
 * registre officiel ({@code cerbere-core}) : le miroir local change d'identité
 * (id de l'orphelin remplacé par {@code coreDeviceId}) pour que la corrélation
 * par id, déjà en place partout ailleurs (voir ADR 0004/0016), continue de
 * fonctionner. {@code label}/{@code zoneId} proviennent du device officiel
 * (source de vérité pour ces champs). Contrat REST partagé entre
 * {@code cerbere-bff} (émetteur) et {@code cerbere-devices-mock} (récepteur).
 */
public record BindSimulatedDeviceRequest(
	@NotBlank String coreDeviceId,
	@NotBlank String label,
	String zoneId
) {
}
