package fr.cerbere.component.cerbere_history.infrastructure.persistence.mongo.alarm;

import fr.cerbere.component.cerbere_history.domain.model.AlarmStateChangeRecord;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;
import fr.cerbere.component.cerbere_history.domain.port.out.alarm.AlarmStateChangeRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation Mongo du port {@link AlarmStateChangeRecordRepository} — voir
 * {@code DeviceEventMongoRepositoryAdapter} pour le choix de {@link MongoTemplate}
 * plutôt qu'un {@code MongoRepository} dérivé.
 */
@Repository
@RequiredArgsConstructor
public class AlarmStateChangeMongoRepositoryAdapter implements AlarmStateChangeRecordRepository {

	private final MongoTemplate mongoTemplate;
	private final AlarmStateChangeMapper alarmStateChangeMapper;

	@Override
	public AlarmStateChangeRecord save(final AlarmStateChangeRecord change) {
		final AlarmStateChangeDocument saved = this.mongoTemplate.save(this.alarmStateChangeMapper.toDocument(change));
		return this.alarmStateChangeMapper.toDomain(saved);
	}

	@Override
	public PageResult<AlarmStateChangeRecord> search(final Instant from, final Instant to, final PageRequest pageRequest) {
		final Criteria criteria = this.buildCriteria(from, to);
		final long totalElements = this.mongoTemplate.count(new Query(criteria), AlarmStateChangeDocument.class);

		final Query pageQuery = new Query(criteria)
			.with(Sort.by(Sort.Direction.DESC, "occurredAt"))
			.skip((long) pageRequest.page() * pageRequest.size())
			.limit(pageRequest.size());
		final List<AlarmStateChangeRecord> content = this.mongoTemplate.find(pageQuery, AlarmStateChangeDocument.class).stream()
			.map(this.alarmStateChangeMapper::toDomain)
			.toList();

		return new PageResult<>(content, totalElements, pageRequest.page(), pageRequest.size());
	}

	private Criteria buildCriteria(final Instant from, final Instant to) {
		final List<Criteria> criteria = new ArrayList<>();
		if (from != null) {
			criteria.add(Criteria.where("occurredAt").gte(from));
		}
		if (to != null) {
			criteria.add(Criteria.where("occurredAt").lte(to));
		}
		return criteria.isEmpty() ? new Criteria() : new Criteria().andOperator(criteria.toArray(new Criteria[0]));
	}
}
