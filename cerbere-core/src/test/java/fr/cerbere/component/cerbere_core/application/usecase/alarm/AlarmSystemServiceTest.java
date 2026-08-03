package fr.cerbere.component.cerbere_core.application.usecase.alarm;

import fr.cerbere.component.cerbere_core.application.service.AlarmTriggerReevaluationService;
import fr.cerbere.component.cerbere_core.domain.event.AlarmStateChanged;
import fr.cerbere.component.cerbere_core.domain.exception.ConcurrentAlarmSystemUpdateException;
import fr.cerbere.component.cerbere_core.domain.model.AlarmMode;
import fr.cerbere.component.cerbere_core.domain.model.AlarmSystem;
import fr.cerbere.component.cerbere_core.domain.model.ArmingMode;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.DeviceType;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmStateChangedPublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmSystemRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlarmSystemServiceTest {

	private InMemoryDeviceRepository deviceRepository;
	private FlakyAlarmSystemRepository alarmSystemRepository;
	private RecordingAlarmStateChangedPublisher alarmStateChangedPublisher;
	private AlarmSystemService service;

	@BeforeEach
	void setUp() {
		this.deviceRepository = new InMemoryDeviceRepository();
		this.alarmSystemRepository = new FlakyAlarmSystemRepository();
		this.alarmStateChangedPublisher = new RecordingAlarmStateChangedPublisher();
		final AlarmTriggerReevaluationService alarmTriggerReevaluationService =
			new AlarmTriggerReevaluationService(this.deviceRepository, this.alarmSystemRepository, this.alarmStateChangedPublisher);
		this.service = new AlarmSystemService(this.alarmSystemRepository, this.alarmStateChangedPublisher, alarmTriggerReevaluationService);
	}

	@Test
	void armShouldSwitchModeWithoutTriggeringWhenNoDeviceViolates() {
		final AlarmSystem armed = this.service.arm(ArmingMode.AWAY);

		assertThat(armed.getMode()).isEqualTo(AlarmMode.ARMED_AWAY);
		assertThat(armed.isTriggered()).isFalse();
	}

	@Test
	void armShouldTriggerImmediatelyWhenADeviceIsAlreadyViolating() {
		this.deviceRepository.save(Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null).withViolation());

		final AlarmSystem armed = this.service.arm(ArmingMode.AWAY);

		assertThat(armed.isTriggered()).isTrue();
	}

	@Test
	void disarmShouldResetModeAndTriggeredState() {
		this.service.arm(ArmingMode.AWAY);
		this.deviceRepository.save(Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null).withViolation());

		final AlarmSystem disarmed = this.service.disarm();

		assertThat(disarmed.getMode()).isEqualTo(AlarmMode.DISARMED);
		assertThat(disarmed.isTriggered()).isFalse();
	}

	@Test
	void getCurrentStatusShouldReturnInitialStateWhenNothingPersistedYet() {
		final AlarmSystem status = this.service.getCurrentStatus();

		assertThat(status.getMode()).isEqualTo(AlarmMode.DISARMED);
		assertThat(status.isTriggered()).isFalse();
	}

	@Test
	void armShouldRetryAndSucceedAfterTransientConcurrentUpdateConflicts() {
		this.alarmSystemRepository.failNextSaves(3);

		final AlarmSystem armed = this.service.arm(ArmingMode.HOME);

		assertThat(armed.getMode()).isEqualTo(AlarmMode.ARMED_HOME);
		assertThat(this.alarmSystemRepository.saveAttempts).isEqualTo(4);
	}

	@Test
	void armShouldGiveUpAfterExhaustingRetriesOnAPersistentConflict() {
		this.alarmSystemRepository.failNextSaves(Integer.MAX_VALUE);

		assertThatThrownBy(() -> this.service.arm(ArmingMode.AWAY))
			.isInstanceOf(ConcurrentAlarmSystemUpdateException.class);
	}

	private static final class InMemoryDeviceRepository implements DeviceRepository {

		private final Map<UUID, Device> devices = new HashMap<>();

		@Override
		public Device save(final Device device) {
			this.devices.put(device.getId(), device);
			return device;
		}

		@Override
		public Optional<Device> findById(final UUID id) {
			return Optional.ofNullable(this.devices.get(id));
		}

		@Override
		public List<Device> findAll() {
			return List.copyOf(this.devices.values());
		}

		@Override
		public List<Device> findByZoneId(final UUID zoneId) {
			return this.devices.values().stream().filter(device -> zoneId.equals(device.getZoneId())).toList();
		}

		@Override
		public Optional<Device> findByLabel(final String label) {
			return this.devices.values().stream().filter(device -> device.getLabel().equals(label)).findFirst();
		}

		@Override
		public void deleteById(final UUID id) {
			this.devices.remove(id);
		}
	}

	/**
	 * Simule le comportement de {@code AlarmSystemMongoRepositoryAdapter} sous
	 * collision de verrouillage optimiste — voir la même double dans
	 * {@code AlarmTriggerReevaluationServiceTest}.
	 */
	private static final class FlakyAlarmSystemRepository implements AlarmSystemRepository {

		private final Map<String, AlarmSystem> systems = new HashMap<>();
		private int failuresRemaining;
		private int saveAttempts;

		void failNextSaves(final int count) {
			this.failuresRemaining = count;
		}

		@Override
		public AlarmSystem save(final AlarmSystem alarmSystem) {
			this.saveAttempts++;
			if (this.failuresRemaining > 0) {
				this.failuresRemaining--;
				throw new ConcurrentAlarmSystemUpdateException(alarmSystem.getId(), null);
			}
			this.systems.put(alarmSystem.getId(), alarmSystem);
			return alarmSystem;
		}

		@Override
		public Optional<AlarmSystem> findById(final String systemId) {
			return Optional.ofNullable(this.systems.get(systemId));
		}
	}

	private static final class RecordingAlarmStateChangedPublisher implements AlarmStateChangedPublisher {

		private final List<AlarmStateChanged> events = new ArrayList<>();

		@Override
		public void publish(final AlarmStateChanged event) {
			this.events.add(event);
		}
	}
}
