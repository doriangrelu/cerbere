package fr.cerbere.component.cerbere_devices_mock.domain.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie : interroge le registre officiel des devices ({@code cerbere-core})
 * pour résoudre le type d'un device existant. Voir ADR 0016 : {@code cerbere-core}
 * est la seule source de vérité pour l'identité d'un device ; {@code cerbere-devices-mock}
 * ne fait que s'y référer pour savoir ce qu'il peut simuler.
 */
public interface CoreDeviceLookupPort {

	/**
	 * @return le nom du type de device (ex. {@code "CONTACT"}), tel qu'exposé par
	 * {@code cerbere-core}, ou vide si aucun device avec cet id n'y est enregistré.
	 */
	Optional<String> findTypeById(UUID deviceId);
}
