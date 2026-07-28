package fr.cerbere.component.cerbere_devices_mock.domain.port.in;

import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;

import java.util.UUID;

/**
 * Port d'entrée : brancher ou débrancher un device simulé du réseau (voir
 * ADR 0024). Un device hors réseau cesse toute émission — de quoi éprouver la
 * supervision de vie de {@code cerbere-core} sans toucher au matériel.
 */
public interface SetDeviceAvailabilityUseCase {

	SimulatedDevice setOnline(UUID id, boolean online);
}
