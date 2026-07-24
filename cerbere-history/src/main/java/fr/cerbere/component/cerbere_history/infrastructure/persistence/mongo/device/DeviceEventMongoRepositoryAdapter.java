package fr.cerbere.component.cerbere_history.infrastructure.persistence.mongo.device;

import fr.cerbere.component.cerbere_history.domain.model.DeviceEventRecord;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;
import fr.cerbere.component.cerbere_history.domain.port.out.device.DeviceEventRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implémentation Mongo du port {@link DeviceEventRecordRepository}. Utilise
 * {@link MongoTemplate} directement (pas de {@code MongoRepository} dérivé) :
 * le filtrage combine jusqu'à 5 critères tous optionnels, ce qu'une méthode de
 * requête dérivée par nom ne peut pas exprimer sans explosion combinatoire.
 */
@Repository
@RequiredArgsConstructor
public class DeviceEventMongoRepositoryAdapter implements DeviceEventRecordRepository {

	private final MongoTemplate mongoTemplate;
	private final DeviceEventMapper deviceEventMapper;

	@Override
	public DeviceEventRecord save(final DeviceEventRecord event) {
		final DeviceEventDocument saved = this.mongoTemplate.save(this.deviceEventMapper.toDocument(event));
		return this.deviceEventMapper.toDomain(saved);
	}

	@Override
	public PageResult<DeviceEventRecord> search(final UUID deviceId, final UUID zoneId, final String eventType,
												 final Instant from, final Instant to, final PageRequest pageRequest) {
		final Criteria criteria = this.buildCriteria(deviceId, zoneId, eventType, from, to);
		final long totalElements = this.mongoTemplate.count(new Query(criteria), DeviceEventDocument.class);

		final Query pageQuery = new Query(criteria)
			.with(Sort.by(Sort.Direction.DESC, "occurredAt"))
			.skip((long) pageRequest.page() * pageRequest.size())
			.limit(pageRequest.size());
		final List<DeviceEventRecord> content = this.mongoTemplate.find(pageQuery, DeviceEventDocument.class).stream()
			.map(this.deviceEventMapper::toDomain)
			.toList();

		return new PageResult<>(content, totalElements, pageRequest.page(), pageRequest.size());
	}

	private Criteria buildCriteria(final UUID deviceId, final UUID zoneId, final String eventType, final Instant from, final Instant to) {
		final List<Criteria> criteria = new ArrayList<>();
		if (deviceId != null) {
			criteria.add(Criteria.where("deviceId").is(deviceId.toString()));
		}
		if (zoneId != null) {
			criteria.add(Criteria.where("zoneId").is(zoneId.toString()));
		}
		if (eventType != null) {
			criteria.add(Criteria.where("eventType").is(eventType));
		}
		if (from != null) {
			criteria.add(Criteria.where("occurredAt").gte(from));
		}
		if (to != null) {
			criteria.add(Criteria.where("occurredAt").lte(to));
		}
		return criteria.isEmpty() ? new Criteria() : new Criteria().andOperator(criteria.toArray(new Criteria[0]));
	}
}
