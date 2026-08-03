package fr.cerbere.component.cerbere_core.application.usecase.alarm;

import fr.cerbere.component.cerbere_core.domain.event.AlertRaised;
import fr.cerbere.component.cerbere_core.domain.event.DeviceSupervisionChanged;
import fr.cerbere.component.cerbere_core.domain.model.AlarmSystem;
import fr.cerbere.component.cerbere_core.domain.model.ArmingMode;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.DeviceEventReport;
import fr.cerbere.component.cerbere_core.domain.model.DeviceType;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmSystemRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlertPublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceSupervisionChangedPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires purs de l'évaluation d'un événement de device — voir ADR 0025
 * pour le fait que ce use-case ne décide plus lui-même du déclenchement de
 * l'alarme (délégué à {@code ReevaluateAlarmStateService} via l'événement
 * {@link DeviceSupervisionChanged}, testé séparément).
 */
class HandleDeviceEventServiceTest {

	private InMemoryDeviceRepository deviceRepository;
	private InMemoryAlarmSystemRepository alarmSystemRepository;
	private RecordingAlertPublisher alertPublisher;
	private RecordingSupervisionChangedPublisher supervisionChangedPublisher;
	private HandleDeviceEventService service;

	@BeforeEach
	void setUp() {
		this.deviceRepository = new InMemoryDeviceRepository();
		this.alarmSystemRepository = new InMemoryAlarmSystemRepository();
		this.alertPublisher = new RecordingAlertPublisher();
		this.supervisionChangedPublisher = new RecordingSupervisionChangedPublisher();
		this.service = new HandleDeviceEventService(
			this.alarmSystemRepository, this.deviceRepository, this.alertPublisher, this.supervisionChangedPublisher);
	}

	@Test
	void handleShouldIgnoreEventForUnknownDevice() {
		this.service.handle(contactEvent(UUID.randomUUID(), "OPEN"));

		assertThat(this.alertPublisher.alerts).isEmpty();
		assertThat(this.supervisionChangedPublisher.events).isEmpty();
	}

	@Test
	void handleShouldRaiseAlertAndMarkViolationWhenArmedAwayAndContactOpens() {
		this.alarmSystemRepository.save(AlarmSystem.initial(AlarmSystem.DEFAULT_SYSTEM_ID).arm(ArmingMode.AWAY));
		final Device device = this.deviceRepository.save(Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null));

		this.service.handle(contactEvent(device.getId(), "OPEN"));

		assertThat(this.deviceRepository.findById(device.getId()).orElseThrow().isViolation()).isTrue();
		assertThat(this.alertPublisher.alerts).hasSize(1);
		assertThat(this.supervisionChangedPublisher.events).hasSize(1);
	}

	@Test
	void handleShouldNotRaiseAlertWhenDisarmedButStillUpdatesDevice() {
		final Device device = this.deviceRepository.save(Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null));

		this.service.handle(contactEvent(device.getId(), "OPEN"));

		assertThat(this.deviceRepository.findById(device.getId()).orElseThrow().isViolation()).isTrue();
		assertThat(this.alertPublisher.alerts).isEmpty();
		assertThat(this.supervisionChangedPublisher.events).hasSize(1);
	}

	@Test
	void handleShouldNotRaiseAlertForDisabledDeviceEvenWhenArmedAndViolating() {
		this.alarmSystemRepository.save(AlarmSystem.initial(AlarmSystem.DEFAULT_SYSTEM_ID).arm(ArmingMode.AWAY));
		final Device device = this.deviceRepository.save(
			Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null).withEnabled(false));

		this.service.handle(contactEvent(device.getId(), "OPEN"));

		assertThat(this.deviceRepository.findById(device.getId()).orElseThrow().isViolation()).isTrue();
		assertThat(this.alertPublisher.alerts).isEmpty();
	}

	@Test
	void handleShouldIgnoreMotionAsAViolationWhenArmedHome() {
		this.alarmSystemRepository.save(AlarmSystem.initial(AlarmSystem.DEFAULT_SYSTEM_ID).arm(ArmingMode.HOME));
		final Device device = this.deviceRepository.save(Device.register(UUID.randomUUID(), DeviceType.MOTION, "Salon", null));

		this.service.handle(motionEvent(device.getId(), true));

		assertThat(this.deviceRepository.findById(device.getId()).orElseThrow().isViolation()).isFalse();
		assertThat(this.alertPublisher.alerts).isEmpty();
	}

	@Test
	void handleShouldTreatMotionAsAViolationWhenArmedAway() {
		this.alarmSystemRepository.save(AlarmSystem.initial(AlarmSystem.DEFAULT_SYSTEM_ID).arm(ArmingMode.AWAY));
		final Device device = this.deviceRepository.save(Device.register(UUID.randomUUID(), DeviceType.MOTION, "Salon", null));

		this.service.handle(motionEvent(device.getId(), true));

		assertThat(this.deviceRepository.findById(device.getId()).orElseThrow().isViolation()).isTrue();
		assertThat(this.alertPublisher.alerts).hasSize(1);
	}

	@Test
	void handleShouldClearViolationOnANonViolatingEvent() {
		final Device device = this.deviceRepository.save(
			Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null).withViolation());

		this.service.handle(contactEvent(device.getId(), "CLOSED"));

		assertThat(this.deviceRepository.findById(device.getId()).orElseThrow().isViolation()).isFalse();
	}

	@Test
	void handleShouldMarkDeviceLinkedOnFirstAcceptedEventAndNeverAgainReset() {
		final Device device = this.deviceRepository.save(Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null));
		assertThat(device.isLinked()).isFalse();

		this.service.handle(contactEvent(device.getId(), "CLOSED"));

		assertThat(this.deviceRepository.findById(device.getId()).orElseThrow().isLinked()).isTrue();
	}

	private static DeviceEventReport contactEvent(final UUID deviceId, final String state) {
		return new DeviceEventReport(
			deviceId, "device.contact.state_changed", Map.of("state", state), Instant.now(), UUID.randomUUID());
	}

	private static DeviceEventReport motionEvent(final UUID deviceId, final boolean detected) {
		return new DeviceEventReport(
			deviceId, "device.motion.detected", Map.of("detected", detected), Instant.now(), UUID.randomUUID());
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

	private static final class RecordingAlertPublisher implements AlertPublisher {

		private final List<AlertRaised> alerts = new ArrayList<>();

		@Override
		public void publish(final AlertRaised event) {
			this.alerts.add(event);
		}
	}

	private static final class RecordingSupervisionChangedPublisher implements DeviceSupervisionChangedPublisher {

		private final List<DeviceSupervisionChanged> events = new ArrayList<>();

		@Override
		public void publish(final DeviceSupervisionChanged event) {
			this.events.add(event);
		}
	}
}
