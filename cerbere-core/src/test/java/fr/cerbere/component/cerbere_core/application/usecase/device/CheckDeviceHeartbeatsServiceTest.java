package fr.cerbere.component.cerbere_core.application.usecase.device;

import fr.cerbere.component.cerbere_core.application.service.AlarmTriggerReevaluationService;
import fr.cerbere.component.cerbere_core.application.service.RecomputeZoneViolationService;
import fr.cerbere.component.cerbere_core.domain.event.AlarmStateChanged;
import fr.cerbere.component.cerbere_core.domain.event.AlertRaised;
import fr.cerbere.component.cerbere_core.domain.model.AlarmSystem;
import fr.cerbere.component.cerbere_core.domain.model.ArmingMode;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.DeviceType;
import fr.cerbere.component.cerbere_core.domain.model.Zone;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmStateChangedPublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmSystemRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlertPublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.zone.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires purs (aucun contexte Spring) de la supervision de vie des
 * devices (voir ADR 0020).
 */
class CheckDeviceHeartbeatsServiceTest {

	private static final Duration TIMEOUT = Duration.ofMinutes(5);

	private InMemoryDeviceRepository deviceRepository;
	private InMemoryZoneRepository zoneRepository;
	private InMemoryAlarmSystemRepository alarmSystemRepository;
	private RecordingAlarmStateChangedPublisher alarmStateChangedPublisher;
	private RecordingAlertPublisher alertPublisher;
	private CheckDeviceHeartbeatsService service;

	@BeforeEach
	void setUp() {
		this.deviceRepository = new InMemoryDeviceRepository();
		this.zoneRepository = new InMemoryZoneRepository();
		this.alarmSystemRepository = new InMemoryAlarmSystemRepository();
		this.alarmStateChangedPublisher = new RecordingAlarmStateChangedPublisher();
		this.alertPublisher = new RecordingAlertPublisher();
		final RecomputeZoneViolationService recomputeZoneViolationService =
			new RecomputeZoneViolationService(this.zoneRepository, this.deviceRepository);
		final AlarmTriggerReevaluationService alarmTriggerReevaluationService =
			new AlarmTriggerReevaluationService(this.deviceRepository, this.alarmSystemRepository, this.alarmStateChangedPublisher);
		this.service = new CheckDeviceHeartbeatsService(
			this.deviceRepository, recomputeZoneViolationService, alarmTriggerReevaluationService, this.alertPublisher, TIMEOUT
		);
	}

	@Test
	void checkShouldMarkStaleEnabledDeviceAsViolatingAndRaiseAlert() {
		final Device device = Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null)
			.withLastSeenAt(Instant.now().minus(Duration.ofMinutes(10)));
		this.deviceRepository.save(device);

		this.service.check();

		final Device saved = this.deviceRepository.findById(device.getId()).orElseThrow();
		assertThat(saved.isViolation()).isTrue();
		assertThat(this.alertPublisher.publishedAlerts()).hasSize(1);
		assertThat(this.alertPublisher.publishedAlerts().getFirst().deviceId()).isEqualTo(device.getId());
	}

	@Test
	void checkShouldTriggerAlarmWhenArmedAndDeviceGoesStale() {
		this.alarmSystemRepository.save(AlarmSystem.initial(AlarmSystem.DEFAULT_SYSTEM_ID).arm(ArmingMode.AWAY));
		final Device device = Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null)
			.withLastSeenAt(Instant.now().minus(Duration.ofMinutes(10)));
		this.deviceRepository.save(device);

		this.service.check();

		final AlarmSystem saved = this.alarmSystemRepository.findById(AlarmSystem.DEFAULT_SYSTEM_ID).orElseThrow();
		assertThat(saved.isTriggered()).isTrue();
		assertThat(this.alarmStateChangedPublisher.publishedEvents()).hasSize(1);
	}

	@Test
	void checkShouldIgnoreDeviceSeenRecently() {
		final Device device = Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null);
		this.deviceRepository.save(device);

		this.service.check();

		assertThat(this.deviceRepository.findById(device.getId()).orElseThrow().isViolation()).isFalse();
		assertThat(this.alertPublisher.publishedAlerts()).isEmpty();
	}

	@Test
	void checkShouldIgnoreDisabledDevice() {
		final Device device = Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null)
			.withEnabled(false)
			.withLastSeenAt(Instant.now().minus(Duration.ofMinutes(10)));
		this.deviceRepository.save(device);

		this.service.check();

		assertThat(this.deviceRepository.findById(device.getId()).orElseThrow().isViolation()).isFalse();
		assertThat(this.alertPublisher.publishedAlerts()).isEmpty();
	}

	@Test
	void checkShouldNotRaiseAlertAgainForDeviceAlreadyMarkedViolating() {
		final Device device = Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null)
			.withViolation()
			.withLastSeenAt(Instant.now().minus(Duration.ofMinutes(10)));
		this.deviceRepository.save(device);

		this.service.check();

		assertThat(this.alertPublisher.publishedAlerts()).isEmpty();
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

	private static final class InMemoryZoneRepository implements ZoneRepository {

		private final Map<UUID, Zone> zones = new HashMap<>();

		@Override
		public Zone save(final Zone zone) {
			this.zones.put(zone.getId(), zone);
			return zone;
		}

		@Override
		public Optional<Zone> findById(final UUID id) {
			return Optional.ofNullable(this.zones.get(id));
		}

		@Override
		public List<Zone> findAll() {
			return List.copyOf(this.zones.values());
		}

		@Override
		public Optional<Zone> findByName(final String name) {
			return this.zones.values().stream().filter(zone -> zone.getName().equals(name)).findFirst();
		}

		@Override
		public void deleteById(final UUID id) {
			this.zones.remove(id);
		}
	}

	private static final class InMemoryAlarmSystemRepository implements AlarmSystemRepository {

		private final Map<String, AlarmSystem> systems = new HashMap<>();

		@Override
		public AlarmSystem save(final AlarmSystem alarmSystem) {
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

		List<AlarmStateChanged> publishedEvents() {
			return this.events;
		}
	}

	private static final class RecordingAlertPublisher implements AlertPublisher {

		private final List<AlertRaised> alerts = new ArrayList<>();

		@Override
		public void publish(final AlertRaised event) {
			this.alerts.add(event);
		}

		List<AlertRaised> publishedAlerts() {
			return this.alerts;
		}
	}
}
