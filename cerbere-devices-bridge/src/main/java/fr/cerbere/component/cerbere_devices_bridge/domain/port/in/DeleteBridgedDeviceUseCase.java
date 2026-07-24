package fr.cerbere.component.cerbere_devices_bridge.domain.port.in;

import java.util.UUID;

/**
 * Port d'entrée : retirer le miroir local d'un device supprimé du registre
 * officiel (`cerbere-core`) — voir ADR 0016.
 */
public interface DeleteBridgedDeviceUseCase {

	void delete(UUID id);
}
