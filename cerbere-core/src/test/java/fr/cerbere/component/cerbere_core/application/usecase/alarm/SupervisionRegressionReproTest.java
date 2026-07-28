package fr.cerbere.component.cerbere_core.application.usecase.alarm;

import fr.cerbere.component.cerbere_core.application.service.AlarmTriggerReevaluationService;
import fr.cerbere.component.cerbere_core.application.service.RecomputeZoneViolationService;
import fr.cerbere.component.cerbere_core.application.usecase.device.CheckDeviceHeartbeatsService;
import fr.cerbere.component.cerbere_core.domain.event.AlarmStateChanged;
import fr.cerbere.component.cerbere_core.domain.event.AlertRaised;
import fr.cerbere.component.cerbere_core.domain.event.DeviceSupervisionChanged;
import fr.cerbere.component.cerbere_core.domain.model.AlarmSystem;
import fr.cerbere.component.cerbere_core.domain.model.ArmingMode;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.DeviceEventReport;
import fr.cerbere.component.cerbere_core.domain.model.DeviceType;
import fr.cerbere.component.cerbere_core.domain.model.Zone;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.ReevaluateAlarmStateUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmStateChangedPublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmSystemRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlertPublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceSupervisionChangedPublisher;
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
 * Reproduit le scénario signalé en recette : device hors réseau -> violation
 * levée par le heartbeat -> désarmement -> device de retour + événement CLOSED
 * -> réarmement -> événement OPEN. L'alarme doit se déclencher sur ce dernier
 * événement, exactement comme sur un premier cycle.
 */
class SupervisionRegressionReproTest {

	private InMemoryDeviceRepository deviceRepository;
	private InMemoryZoneRepository zoneRepository;
	private InMemoryAlarmSystemRepository alarmSystemRepository;
	private RecordingAlarmStateChangedPublisher alarmStateChangedPublisher;
	private RecordingAlertPublisher alertPublisher;
	private HandleDeviceEventService handleDeviceEventService;
	private CheckDeviceHeartbeatsService checkDeviceHeartbeatsService;
	private AlarmSystemService alarmSystemService;

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
		final ReevaluateAlarmStateUseCase reevaluateAlarmStateUseCase =
			new ReevaluateAlarmStateService(recomputeZoneViolationService, alarmTriggerReevaluationService);
		final DeviceSupervisionChangedPublisher supervisionChangedPublisher =
			event -> reevaluateAlarmStateUseCase.reevaluate(event);

		this.handleDeviceEventService = new HandleDeviceEventService(
			this.alarmSystemRepository, this.deviceRepository, this.alertPublisher, supervisionChangedPublisher);
		this.checkDeviceHeartbeatsService = new CheckDeviceHeartbeatsService(
			this.deviceRepository, supervisionChangedPublisher, this.alertPublisher, Duration.ofMinutes(5));
		this.alarmSystemService = new AlarmSystemService(
			this.alarmSystemRepository, this.alarmStateChangedPublisher, alarmTriggerReevaluationService);
	}

	@Test
	void reArmingAfterAHeartbeatRecoveryShouldStillTriggerOnANewViolation() {
		final UUID deviceId = UUID.randomUUID();
		Device device = Device.register(deviceId, DeviceType.CONTACT, "Porte", null).withLinked();
		this.deviceRepository.save(device);

		this.alarmSystemService.arm(ArmingMode.AWAY);

		device = this.deviceRepository.findById(deviceId).orElseThrow()
			.withLastSeenAt(Instant.now().minus(Duration.ofMinutes(10)));
		this.deviceRepository.save(device);
		this.checkDeviceHeartbeatsService.check();

		assertThat(this.deviceRepository.findById(deviceId).orElseThrow().isViolation()).isTrue();
		assertThat(this.alarmSystemRepository.findById(AlarmSystem.DEFAULT_SYSTEM_ID).orElseThrow().isTriggered()).isTrue();

		this.alarmSystemService.disarm();

		this.handleDeviceEventService.handle(new DeviceEventReport(
			deviceId, "device.contact.state_changed", Map.of("state", "CLOSED"), Instant.now(), UUID.randomUUID()));

		assertThat(this.deviceRepository.findById(deviceId).orElseThrow().isViolation())
			.as("le device doit ressortir de violation une fois un événement CLOSED reçu")
			.isFalse();

		this.alarmSystemService.arm(ArmingMode.AWAY);
		assertThat(this.alarmSystemRepository.findById(AlarmSystem.DEFAULT_SYSTEM_ID).orElseThrow().isTriggered()).isFalse();

		this.handleDeviceEventService.handle(new DeviceEventReport(
			deviceId, "device.contact.state_changed", Map.of("state", "OPEN"), Instant.now(), UUID.randomUUID()));

		assertThat(this.deviceRepository.findById(deviceId).orElseThrow().isViolation()).isTrue();
		assertThat(this.alarmSystemRepository.findById(AlarmSystem.DEFAULT_SYSTEM_ID).orElseThrow().isTriggered())
			.as("l'alarme doit se déclencher sur la nouvelle violation OPEN après réarmement")
			.isTrue();
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
	}

	private static final class RecordingAlertPublisher implements AlertPublisher {

		private final List<AlertRaised> alerts = new ArrayList<>();

		@Override
		public void publish(final AlertRaised event) {
			this.alerts.add(event);
		}
	}
}
