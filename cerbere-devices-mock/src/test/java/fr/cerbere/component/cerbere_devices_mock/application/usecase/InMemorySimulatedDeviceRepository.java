package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Doublure de test du port de persistance, partagée par les tests de use-cases
 * du module. {@link LinkedHashMap} pour que l'ordre d'insertion soit stable et
 * les assertions déterministes.
 */
final class InMemorySimulatedDeviceRepository implements SimulatedDeviceRepository {

	private final Map<UUID, SimulatedDevice> devices = new LinkedHashMap<>();

	@Override
	public SimulatedDevice save(final SimulatedDevice device) {
		this.devices.put(device.getId(), device);
		return device;
	}

	@Override
	public Optional<SimulatedDevice> findById(final UUID id) {
		return Optional.ofNullable(this.devices.get(id));
	}

	@Override
	public Optional<SimulatedDevice> findByFriendlyName(final String friendlyName) {
		return this.devices.values().stream().filter(device -> device.getFriendlyName().equals(friendlyName)).findFirst();
	}

	@Override
	public List<SimulatedDevice> findAll() {
		return List.copyOf(this.devices.values());
	}

	@Override
	public List<SimulatedDevice> findByOnlineTrue() {
		return this.devices.values().stream().filter(SimulatedDevice::isOnline).toList();
	}

	@Override
	public void deleteById(final UUID id) {
		this.devices.remove(id);
	}
}
