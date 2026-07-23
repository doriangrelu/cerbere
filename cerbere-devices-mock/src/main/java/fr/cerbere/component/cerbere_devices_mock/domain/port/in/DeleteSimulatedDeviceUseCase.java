package fr.cerbere.component.cerbere_devices_mock.domain.port.in;

import java.util.UUID;

/**
 * Port d'entrée : retirer le miroir local d'un device supprimé du registre
 * officiel ({@code cerbere-core}) — voir ADR 0016.
 */
public interface DeleteSimulatedDeviceUseCase {

	void delete(UUID id);
}
