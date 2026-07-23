package fr.cerbere.component.cerbere_devices_mock.domain.port.in;

import java.util.UUID;

/**
 * Port d'entrée : aligner le libellé/zone d'un device simulé sur celui du
 * registre officiel ({@code cerbere-core}) suite à une modification — voir ADR 0016.
 */
public interface UpdateSimulatedDeviceUseCase {

	void update(UUID id, String label, UUID zoneId);
}
