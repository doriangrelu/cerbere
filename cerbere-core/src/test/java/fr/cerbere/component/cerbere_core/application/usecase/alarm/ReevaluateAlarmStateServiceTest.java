package fr.cerbere.component.cerbere_core.application.usecase.alarm;

import fr.cerbere.component.cerbere_core.application.service.AlarmTriggerReevaluationService;
import fr.cerbere.component.cerbere_core.application.service.RecomputeZoneViolationService;
import fr.cerbere.component.cerbere_core.domain.event.AlarmStateChanged;
import fr.cerbere.component.cerbere_core.domain.event.DeviceSupervisionChanged;
import fr.cerbere.component.cerbere_core.domain.model.AlarmSystem;
import fr.cerbere.component.cerbere_core.domain.model.ArmingMode;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.DeviceType;
import fr.cerbere.component.cerbere_core.domain.model.Zone;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmStateChangedPublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmSystemRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.zone.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires purs (aucun contexte Spring) du seul point du module qui
 * dérive l'état d'alarme de l'état des devices — voir ADR 0025.
 */
class ReevaluateAlarmStateServiceTest {

	private InMemoryDeviceRepository deviceRepository;
	private InMemoryZoneRepository zoneRepository;
	private InMemoryAlarmSystemRepository alarmSystemRepository;
	private RecordingAlarmStateChangedPublisher alarmStateChangedPublisher;
	private ReevaluateAlarmStateService service;

	@BeforeEach
	void setUp() {
		this.deviceRepository = new InMemoryDeviceRepository();
		this.zoneRepository = new InMemoryZoneRepository();
		this.alarmSystemRepository = new InMemoryAlarmSystemRepository();
		this.alarmStateChangedPublisher = new RecordingAlarmStateChangedPublisher();
		this.service = new ReevaluateAlarmStateService(
			new RecomputeZoneViolationService(this.zoneRepository, this.deviceRepository),
			new AlarmTriggerReevaluationService(this.deviceRepository, this.alarmSystemRepository, this.alarmStateChangedPublisher)
		);
	}

	@Test
	void reevaluateShouldMarkAffectedZoneAsViolatingWhenOneOfItsDevicesViolates() {
		final Zone zone = this.zoneRepository.save(Zone.register("Rez-de-chaussée"));
		final Device device = this.deviceRepository.save(
			Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", zone.getId()).withViolation());

		this.service.reevaluate(DeviceSupervisionChanged.forDevice(device.getId(), zone.getId()));

		assertThat(this.zoneRepository.findById(zone.getId()).orElseThrow().isViolation()).isTrue();
	}

	@Test
	void reevaluateShouldClearPreviousZoneWhenDeviceMovedAway() {
		final Zone previousZone = this.zoneRepository.save(Zone.register("Étage").withViolation());
		final Zone newZone = this.zoneRepository.save(Zone.register("Garage"));
		final Device device = this.deviceRepository.save(
			Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", newZone.getId()));

		this.service.reevaluate(DeviceSupervisionChanged.forDevice(device.getId(), previousZone.getId(), newZone.getId()));

		assertThat(this.zoneRepository.findById(previousZone.getId()).orElseThrow().isViolation()).isFalse();
		assertThat(this.zoneRepository.findById(newZone.getId()).orElseThrow().isViolation()).isFalse();
	}

	@Test
	void reevaluateShouldTriggerAlarmWhenArmedAndEnabledDeviceViolates() {
		this.alarmSystemRepository.save(AlarmSystem.initial(AlarmSystem.DEFAULT_SYSTEM_ID).arm(ArmingMode.AWAY));
		final Device device = this.deviceRepository.save(
			Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null).withViolation());

		this.service.reevaluate(DeviceSupervisionChanged.forDevice(device.getId()));

		assertThat(this.alarmSystemRepository.findById(AlarmSystem.DEFAULT_SYSTEM_ID).orElseThrow().isTriggered()).isTrue();
		assertThat(this.alarmStateChangedPublisher.publishedEvents()).hasSize(1);
	}

	@Test
	void reevaluateShouldNotTriggerAlarmWhenDisarmed() {
		final Device device = this.deviceRepository.save(
			Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null).withViolation());

		this.service.reevaluate(DeviceSupervisionChanged.forDevice(device.getId()));

		assertThat(this.alarmSystemRepository.findById(AlarmSystem.DEFAULT_SYSTEM_ID)).isEmpty();
		assertThat(this.alarmStateChangedPublisher.publishedEvents()).isEmpty();
	}

	@Test
	void reevaluateShouldNotTriggerAlarmForDisabledViolatingDevice() {
		this.alarmSystemRepository.save(AlarmSystem.initial(AlarmSystem.DEFAULT_SYSTEM_ID).arm(ArmingMode.AWAY));
		final Device device = this.deviceRepository.save(
			Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null).withViolation().withEnabled(false));

		this.service.reevaluate(DeviceSupervisionChanged.forDevice(device.getId()));

		assertThat(this.alarmSystemRepository.findById(AlarmSystem.DEFAULT_SYSTEM_ID).orElseThrow().isTriggered()).isFalse();
		assertThat(this.alarmStateChangedPublisher.publishedEvents()).isEmpty();
	}

	@Test
	void reevaluateShouldNotPublishTwiceWhenAlarmAlreadyTriggered() {
		this.alarmSystemRepository.save(AlarmSystem.initial(AlarmSystem.DEFAULT_SYSTEM_ID).arm(ArmingMode.AWAY).trigger());
		final Device device = this.deviceRepository.save(
			Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null).withViolation());

		this.service.reevaluate(DeviceSupervisionChanged.forDevice(device.getId()));

		assertThat(this.alarmStateChangedPublisher.publishedEvents()).isEmpty();
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
}
