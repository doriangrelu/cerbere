package fr.cerbere.shared.dto.devicemock;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Requête de création d'un device simulé "brut", sans lien avec le registre
 * officiel ({@code cerbere-core}) : id généré par {@code cerbere-devices-mock},
 * pas de zone (propriété du registre officiel, affectée au moment du binding).
 * Reste orphelin tant qu'il n'est pas lié à un device du registre officiel via
 * {@link BindSimulatedDeviceRequest}. Contrat REST partagé entre
 * {@code cerbere-bff} (émetteur) et {@code cerbere-devices-mock} (récepteur).
 */
public record RegisterOrphanSimulatedDeviceRequest(
	@NotNull String type,
	@NotBlank String label
) {
}
