package fr.cerbere.component.cerbere_core.infrastructure.persistence.mongo.device;

import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation Mongo du port {@link DeviceRepository}.
 */
@Repository
@RequiredArgsConstructor
public class DeviceMongoRepositoryAdapter implements DeviceRepository {

	private final DeviceMongoRepository mongoRepository;
	private final DeviceMapper deviceMapper;

	@Override
	public Device save(final Device device) {
		final DeviceDocument saved = this.mongoRepository.save(this.deviceMapper.toDocument(device));
		return this.deviceMapper.toDomain(saved);
	}

	@Override
	public Optional<Device> findById(final UUID id) {
		return this.mongoRepository.findById(id.toString()).map(this.deviceMapper::toDomain);
	}

	@Override
	public List<Device> findAll() {
		return this.mongoRepository.findAll().stream()
			.map(this.deviceMapper::toDomain)
			.toList();
	}

	@Override
	public List<Device> findByZoneId(final UUID zoneId) {
		return this.mongoRepository.findByZoneId(zoneId.toString()).stream()
			.map(this.deviceMapper::toDomain)
			.toList();
	}

	@Override
	public Optional<Device> findByLabel(final String label) {
		return this.mongoRepository.findByLabel(label).map(this.deviceMapper::toDomain);
	}

	@Override
	public void deleteById(final UUID id) {
		this.mongoRepository.deleteById(id.toString());
	}
}
