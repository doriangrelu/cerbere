package fr.cerbere.component.cerbere_devices_bridge.domain.port.in;

import java.util.UUID;

/**
 * Port d'entrée : aligner le libellé/zone d'un device physique sur celui du
 * registre officiel (`cerbere-core`) suite à une modification — voir ADR 0016.
 */
public interface UpdateBridgedDeviceUseCase {

	void update(UUID id, String label, UUID zoneId);
}
