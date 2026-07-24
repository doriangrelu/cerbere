package fr.cerbere.component.cerbere_devices_bridge.infrastructure.persistence.mongo;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.BridgedDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.BridgedDeviceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation Mongo du port {@link BridgedDeviceRepository}.
 */
@Repository
public class BridgedDeviceMongoRepositoryAdapter implements BridgedDeviceRepository {

	private final BridgedDeviceMongoRepository mongoRepository;

	public BridgedDeviceMongoRepositoryAdapter(final BridgedDeviceMongoRepository mongoRepository) {
		this.mongoRepository = mongoRepository;
	}

	@Override
	public BridgedDevice save(final BridgedDevice device) {
		final BridgedDeviceDocument saved = this.mongoRepository.save(BridgedDeviceMapper.toDocument(device));
		return BridgedDeviceMapper.toDomain(saved);
	}

	@Override
	public Optional<BridgedDevice> findById(final UUID id) {
		return this.mongoRepository.findById(id.toString()).map(BridgedDeviceMapper::toDomain);
	}

	@Override
	public List<BridgedDevice> findAll() {
		return this.mongoRepository.findAll().stream()
			.map(BridgedDeviceMapper::toDomain)
			.toList();
	}

	@Override
	public List<BridgedDevice> findByType(final DeviceType type) {
		return this.mongoRepository.findByType(type.name()).stream()
			.map(BridgedDeviceMapper::toDomain)
			.toList();
	}

	@Override
	public void deleteById(final UUID id) {
		this.mongoRepository.deleteById(id.toString());
	}
}
