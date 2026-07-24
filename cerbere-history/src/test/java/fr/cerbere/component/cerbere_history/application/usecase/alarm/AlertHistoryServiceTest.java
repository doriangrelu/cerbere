package fr.cerbere.component.cerbere_history.application.usecase.alarm;

import fr.cerbere.component.cerbere_history.domain.model.AlertRecord;
import fr.cerbere.component.cerbere_history.domain.model.AlertSeverity;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;
import fr.cerbere.component.cerbere_history.domain.port.out.alarm.AlertRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires purs du service d'historisation/consultation des alertes.
 */
class AlertHistoryServiceTest {

	private RecordingAlertRecordRepository repository;
	private AlertHistoryService service;

	@BeforeEach
	void setUp() {
		this.repository = new RecordingAlertRecordRepository();
		this.service = new AlertHistoryService(this.repository);
	}

	@Test
	void recordShouldSaveTheAlert() {
		final AlertRecord alert = anAlert();

		this.service.record(alert);

		assertThat(this.repository.saved()).containsExactly(alert);
	}

	@Test
	void listShouldDelegateToRepositorySearchAndReturnItsResult() {
		final UUID deviceId = UUID.randomUUID();
		final UUID zoneId = UUID.randomUUID();
		final Instant from = Instant.now().minusSeconds(60);
		final Instant to = Instant.now();
		final PageRequest pageRequest = new PageRequest(0, 20);
		final PageResult<AlertRecord> expected = new PageResult<>(List.of(anAlert()), 1, 0, 20);
		this.repository.nextSearchResult = expected;

		final PageResult<AlertRecord> result = this.service.list(deviceId, zoneId, AlertSeverity.CRITICAL, from, to, pageRequest);

		assertThat(result).isEqualTo(expected);
		assertThat(this.repository.lastSearchArgs()).containsExactly(deviceId, zoneId, AlertSeverity.CRITICAL, from, to, pageRequest);
	}

	private static AlertRecord anAlert() {
		return new AlertRecord(
			UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), AlertSeverity.CRITICAL,
			"Violation detected on Porte d'entrée", Instant.now(), Instant.now(), UUID.randomUUID()
		);
	}

	private static final class RecordingAlertRecordRepository implements AlertRecordRepository {

		private final List<AlertRecord> saved = new ArrayList<>();
		private List<Object> lastSearchArgs;
		private PageResult<AlertRecord> nextSearchResult;

		@Override
		public AlertRecord save(final AlertRecord alert) {
			this.saved.add(alert);
			return alert;
		}

		@Override
		public PageResult<AlertRecord> search(final UUID deviceId, final UUID zoneId, final AlertSeverity severity,
											   final Instant from, final Instant to, final PageRequest pageRequest) {
			this.lastSearchArgs = List.of(deviceId, zoneId, severity, from, to, pageRequest);
			return this.nextSearchResult;
		}

		List<AlertRecord> saved() {
			return this.saved;
		}

		List<Object> lastSearchArgs() {
			return this.lastSearchArgs;
		}
	}
}
