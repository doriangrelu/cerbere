package fr.cerbere.component.cerbere_devices_mock.domain.port.in;

import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;

import java.util.UUID;

/**
 * Port d'entrée : lier un device simulé orphelin (créé sans lien avec le
 * registre officiel) à un device déjà créé dans {@code cerbere-core}. Le
 * miroir local change d'identité (id de l'orphelin remplacé par
 * {@code coreDeviceId}) pour que la corrélation par id continue de
 * fonctionner (voir ADR 0004/0016/0020).
 */
public interface BindSimulatedDeviceUseCase {

	SimulatedDevice bind(UUID orphanId, UUID coreDeviceId, String label, UUID zoneId);
}
