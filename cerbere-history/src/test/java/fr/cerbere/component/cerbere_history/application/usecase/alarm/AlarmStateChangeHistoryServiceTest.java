package fr.cerbere.component.cerbere_history.application.usecase.alarm;

import fr.cerbere.component.cerbere_history.domain.model.AlarmMode;
import fr.cerbere.component.cerbere_history.domain.model.AlarmStateChangeRecord;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;
import fr.cerbere.component.cerbere_history.domain.port.out.alarm.AlarmStateChangeRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires purs du service d'historisation/consultation des
 * changements d'état de l'alarme.
 */
class AlarmStateChangeHistoryServiceTest {

	private RecordingAlarmStateChangeRecordRepository repository;
	private AlarmStateChangeHistoryService service;

	@BeforeEach
	void setUp() {
		this.repository = new RecordingAlarmStateChangeRecordRepository();
		this.service = new AlarmStateChangeHistoryService(this.repository);
	}

	@Test
	void recordShouldSaveTheChange() {
		final AlarmStateChangeRecord change = aChange();

		this.service.record(change);

		assertThat(this.repository.saved()).containsExactly(change);
	}

	@Test
	void listShouldDelegateToRepositorySearchAndReturnItsResult() {
		final Instant from = Instant.now().minusSeconds(60);
		final Instant to = Instant.now();
		final PageRequest pageRequest = new PageRequest(0, 20);
		final PageResult<AlarmStateChangeRecord> expected = new PageResult<>(List.of(aChange()), 1, 0, 20);
		this.repository.nextSearchResult = expected;

		final PageResult<AlarmStateChangeRecord> result = this.service.list(from, to, pageRequest);

		assertThat(result).isEqualTo(expected);
		assertThat(this.repository.lastSearchArgs()).containsExactly(from, to, pageRequest);
	}

	private static AlarmStateChangeRecord aChange() {
		return new AlarmStateChangeRecord(
			UUID.randomUUID(), AlarmMode.DISARMED, AlarmMode.ARMED_AWAY, false, Instant.now(), Instant.now(), UUID.randomUUID()
		);
	}

	private static final class RecordingAlarmStateChangeRecordRepository implements AlarmStateChangeRecordRepository {

		private final List<AlarmStateChangeRecord> saved = new ArrayList<>();
		private List<Object> lastSearchArgs;
		private PageResult<AlarmStateChangeRecord> nextSearchResult;

		@Override
		public AlarmStateChangeRecord save(final AlarmStateChangeRecord change) {
			this.saved.add(change);
			return change;
		}

		@Override
		public PageResult<AlarmStateChangeRecord> search(final Instant from, final Instant to, final PageRequest pageRequest) {
			this.lastSearchArgs = List.of(from, to, pageRequest);
			return this.nextSearchResult;
		}

		List<AlarmStateChangeRecord> saved() {
			return this.saved;
		}

		List<Object> lastSearchArgs() {
			return this.lastSearchArgs;
		}
	}
}
