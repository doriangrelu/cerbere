package fr.cerbere.component.cerbere_history.infrastructure.persistence.mongo.alarm;

import fr.cerbere.component.cerbere_history.domain.model.AlertRecord;
import fr.cerbere.component.cerbere_history.domain.model.AlertSeverity;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;
import fr.cerbere.component.cerbere_history.domain.port.out.alarm.AlertRecordRepository;
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
 * Implémentation Mongo du port {@link AlertRecordRepository} — voir
 * {@code DeviceEventMongoRepositoryAdapter} pour le choix de {@link MongoTemplate}
 * plutôt qu'un {@code MongoRepository} dérivé.
 */
@Repository
@RequiredArgsConstructor
public class AlertMongoRepositoryAdapter implements AlertRecordRepository {

	private final MongoTemplate mongoTemplate;
	private final AlertMapper alertMapper;

	@Override
	public AlertRecord save(final AlertRecord alert) {
		final AlertDocument saved = this.mongoTemplate.save(this.alertMapper.toDocument(alert));
		return this.alertMapper.toDomain(saved);
	}

	@Override
	public PageResult<AlertRecord> search(final UUID deviceId, final UUID zoneId, final AlertSeverity severity,
										   final Instant from, final Instant to, final PageRequest pageRequest) {
		final Criteria criteria = this.buildCriteria(deviceId, zoneId, severity, from, to);
		final long totalElements = this.mongoTemplate.count(new Query(criteria), AlertDocument.class);

		final Query pageQuery = new Query(criteria)
			.with(Sort.by(Sort.Direction.DESC, "occurredAt"))
			.skip((long) pageRequest.page() * pageRequest.size())
			.limit(pageRequest.size());
		final List<AlertRecord> content = this.mongoTemplate.find(pageQuery, AlertDocument.class).stream()
			.map(this.alertMapper::toDomain)
			.toList();

		return new PageResult<>(content, totalElements, pageRequest.page(), pageRequest.size());
	}

	private Criteria buildCriteria(final UUID deviceId, final UUID zoneId, final AlertSeverity severity, final Instant from, final Instant to) {
		final List<Criteria> criteria = new ArrayList<>();
		if (deviceId != null) {
			criteria.add(Criteria.where("deviceId").is(deviceId.toString()));
		}
		if (zoneId != null) {
			criteria.add(Criteria.where("zoneId").is(zoneId.toString()));
		}
		if (severity != null) {
			criteria.add(Criteria.where("severity").is(severity.name()));
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
