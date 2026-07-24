package fr.cerbere.component.cerbere_history.application.usecase.device;

import fr.cerbere.component.cerbere_history.domain.model.DeviceEventRecord;
import fr.cerbere.component.cerbere_history.domain.model.PageRequest;
import fr.cerbere.component.cerbere_history.domain.model.PageResult;
import fr.cerbere.component.cerbere_history.domain.port.out.device.DeviceEventRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires purs (aucun contexte Spring) du service d'historisation/
 * consultation des événements de device.
 */
class DeviceEventHistoryServiceTest {

	private RecordingDeviceEventRecordRepository repository;
	private DeviceEventHistoryService service;

	@BeforeEach
	void setUp() {
		this.repository = new RecordingDeviceEventRecordRepository();
		this.service = new DeviceEventHistoryService(this.repository);
	}

	@Test
	void recordShouldSaveTheEvent() {
		final DeviceEventRecord event = aDeviceEventRecord();

		this.service.record(event);

		assertThat(this.repository.saved()).containsExactly(event);
	}

	@Test
	void listShouldDelegateToRepositorySearchAndReturnItsResult() {
		final UUID deviceId = UUID.randomUUID();
		final UUID zoneId = UUID.randomUUID();
		final Instant from = Instant.now().minusSeconds(60);
		final Instant to = Instant.now();
		final PageRequest pageRequest = new PageRequest(0, 20);
		final PageResult<DeviceEventRecord> expected = new PageResult<>(List.of(aDeviceEventRecord()), 1, 0, 20);
		this.repository.nextSearchResult = expected;

		final PageResult<DeviceEventRecord> result = this.service.list(deviceId, zoneId, "device.contact.state_changed", from, to, pageRequest);

		assertThat(result).isEqualTo(expected);
		assertThat(this.repository.lastSearchArgs()).containsExactly(deviceId, zoneId, "device.contact.state_changed", from, to, pageRequest);
	}

	private static DeviceEventRecord aDeviceEventRecord() {
		return new DeviceEventRecord(
			UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "device.contact.state_changed",
			Map.of("state", "OPEN"), Instant.now(), Instant.now(), UUID.randomUUID()
		);
	}

	private static final class RecordingDeviceEventRecordRepository implements DeviceEventRecordRepository {

		private final List<DeviceEventRecord> saved = new ArrayList<>();
		private List<Object> lastSearchArgs;
		private PageResult<DeviceEventRecord> nextSearchResult;

		@Override
		public DeviceEventRecord save(final DeviceEventRecord event) {
			this.saved.add(event);
			return event;
		}

		@Override
		public PageResult<DeviceEventRecord> search(final UUID deviceId, final UUID zoneId, final String eventType,
													 final Instant from, final Instant to, final PageRequest pageRequest) {
			this.lastSearchArgs = List.of(deviceId, zoneId, eventType, from, to, pageRequest);
			return this.nextSearchResult;
		}

		List<DeviceEventRecord> saved() {
			return this.saved;
		}

		List<Object> lastSearchArgs() {
			return this.lastSearchArgs;
		}
	}
}
