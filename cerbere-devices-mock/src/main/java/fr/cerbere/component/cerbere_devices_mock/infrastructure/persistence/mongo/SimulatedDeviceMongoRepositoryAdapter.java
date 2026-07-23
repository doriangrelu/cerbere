package fr.cerbere.component.cerbere_devices_mock.infrastructure.persistence.mongo;

import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation Mongo du port {@link SimulatedDeviceRepository}.
 */
@Repository
public class SimulatedDeviceMongoRepositoryAdapter implements SimulatedDeviceRepository {

	private final SimulatedDeviceMongoRepository mongoRepository;

	public SimulatedDeviceMongoRepositoryAdapter(final SimulatedDeviceMongoRepository mongoRepository) {
		this.mongoRepository = mongoRepository;
	}

	@Override
	public SimulatedDevice save(final SimulatedDevice device) {
		final SimulatedDeviceDocument saved = this.mongoRepository.save(SimulatedDeviceMapper.toDocument(device));
		return SimulatedDeviceMapper.toDomain(saved);
	}

	@Override
	public Optional<SimulatedDevice> findById(final UUID id) {
		return this.mongoRepository.findById(id.toString()).map(SimulatedDeviceMapper::toDomain);
	}

	@Override
	public List<SimulatedDevice> findAll() {
		return this.mongoRepository.findAll().stream()
			.map(SimulatedDeviceMapper::toDomain)
			.toList();
	}

	@Override
	public List<SimulatedDevice> findByAutoSimulateTrue() {
		return this.mongoRepository.findByAutoSimulate(true).stream()
			.map(SimulatedDeviceMapper::toDomain)
			.toList();
	}

	@Override
	public void deleteById(final UUID id) {
		this.mongoRepository.deleteById(id.toString());
	}
}
