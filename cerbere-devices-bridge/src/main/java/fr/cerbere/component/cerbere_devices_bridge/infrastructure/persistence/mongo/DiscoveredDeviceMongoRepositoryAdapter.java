package fr.cerbere.component.cerbere_devices_bridge.infrastructure.persistence.mongo;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.DiscoveredDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DiscoveredDeviceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Implémentation Mongo du port {@link DiscoveredDeviceRepository}.
 */
@Repository
public class DiscoveredDeviceMongoRepositoryAdapter implements DiscoveredDeviceRepository {

	private final DiscoveredDeviceMongoRepository mongoRepository;

	public DiscoveredDeviceMongoRepositoryAdapter(final DiscoveredDeviceMongoRepository mongoRepository) {
		this.mongoRepository = mongoRepository;
	}

	@Override
	public DiscoveredDevice save(final DiscoveredDevice device) {
		final DiscoveredDeviceDocument saved = this.mongoRepository.save(DiscoveredDeviceMapper.toDocument(device));
		return DiscoveredDeviceMapper.toDomain(saved);
	}

	@Override
	public Optional<DiscoveredDevice> findByFriendlyName(final String friendlyName) {
		return this.mongoRepository.findById(friendlyName).map(DiscoveredDeviceMapper::toDomain);
	}

	@Override
	public List<DiscoveredDevice> findAll() {
		return this.mongoRepository.findAll().stream()
			.map(DiscoveredDeviceMapper::toDomain)
			.toList();
	}

	@Override
	public void deleteByFriendlyName(final String friendlyName) {
		this.mongoRepository.deleteById(friendlyName);
	}
}
