package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.SetDeviceAvailabilityUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;

import java.util.UUID;

/**
 * Branche ou débranche un device simulé du réseau (voir ADR 0024). Rien n'est
 * publié sur MQTT au moment du changement : un device qui disparaît du réseau
 * ne prévient personne, c'est précisément l'absence d'émission ultérieure qui
 * doit être détectée par la supervision de vie de {@code cerbere-core}.
 */
public final class SetDeviceAvailabilityService implements SetDeviceAvailabilityUseCase {

	private final SimulatedDeviceRepository simulatedDeviceRepository;

	public SetDeviceAvailabilityService(final SimulatedDeviceRepository simulatedDeviceRepository) {
		this.simulatedDeviceRepository = simulatedDeviceRepository;
	}

	@Override
	public SimulatedDevice setOnline(final UUID id, final boolean online) {
		final SimulatedDevice device = this.simulatedDeviceRepository.findById(id)
			.orElseThrow(() -> new DeviceNotFoundException(id));
		return this.simulatedDeviceRepository.save(device.withOnline(online));
	}
}
