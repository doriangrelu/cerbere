package fr.cerbere.component.cerbere_core.application.service;

import fr.cerbere.component.cerbere_core.domain.event.AlarmStateChanged;
import fr.cerbere.component.cerbere_core.domain.exception.ConcurrentAlarmSystemUpdateException;
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

/**
 * {@code AlarmSystem} est un document unique écrit à la fois par le scheduler
 * de supervision de vie et par le traitement de chaque événement de device
 * (voir ADR 0025) : une collision de verrouillage optimiste est attendue, pas
 * exceptionnelle. Ces tests vérifient que {@link AlarmTriggerReevaluationService#reevaluate()}
 * absorbe un nombre borné de collisions en relisant l'état courant, plutôt que
 * de laisser filer silencieusement un déclenchement manqué.
 */
class AlarmTriggerReevaluationServiceTest {

	private InMemoryDeviceRepository deviceRepository;
	private FlakyAlarmSystemRepository alarmSystemRepository;
	private RecordingAlarmStateChangedPublisher alarmStateChangedPublisher;
	private AlarmTriggerReevaluationService service;

	@BeforeEach
	void setUp() {
		this.deviceRepository = new InMemoryDeviceRepository();
		this.alarmSystemRepository = new FlakyAlarmSystemRepository();
		this.alarmStateChangedPublisher = new RecordingAlarmStateChangedPublisher();
		this.service = new AlarmTriggerReevaluationService(
			this.deviceRepository, this.alarmSystemRepository, this.alarmStateChangedPublisher);
	}

	@Test
	void reevaluateShouldRetryAndSucceedAfterTransientConcurrentUpdateConflicts() {
		this.alarmSystemRepository.seed(AlarmSystem.initial(AlarmSystem.DEFAULT_SYSTEM_ID).arm(ArmingMode.AWAY));
		this.deviceRepository.save(Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null).withViolation());
		this.alarmSystemRepository.failNextSaves(3);

		this.service.reevaluate();

		assertThat(this.alarmSystemRepository.findById(AlarmSystem.DEFAULT_SYSTEM_ID).orElseThrow().isTriggered()).isTrue();
		assertThat(this.alarmStateChangedPublisher.publishedEvents()).hasSize(1);
		assertThat(this.alarmSystemRepository.saveAttempts()).isEqualTo(4);
	}

	@Test
	void reevaluateShouldGiveUpAfterExhaustingRetriesOnPersistentConflict() {
		this.alarmSystemRepository.seed(AlarmSystem.initial(AlarmSystem.DEFAULT_SYSTEM_ID).arm(ArmingMode.AWAY));
		this.deviceRepository.save(Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null).withViolation());
		this.alarmSystemRepository.failNextSaves(Integer.MAX_VALUE);

		assertThatThrownBy(() -> this.service.reevaluate())
			.isInstanceOf(ConcurrentAlarmSystemUpdateException.class);
	}

	/**
	 * Reproduit le cas explicitement revendiqué par la JavaDoc de la classe :
	 * un humain désarme le système exactement entre deux tentatives. La
	 * collision qui fait échouer la première tentative simule cette écriture
	 * concurrente (le désarmement gagne la course) ; la seconde tentative doit
	 * relire cet état frais et renoncer, pas écraser le désarmement avec la
	 * décision périmée "il faut déclencher".
	 */
	@Test
	void reevaluateShouldNotTriggerWhenTheSystemIsDisarmedByAConcurrentWriterBetweenRetries() {
		this.alarmSystemRepository.seed(AlarmSystem.initial(AlarmSystem.DEFAULT_SYSTEM_ID).arm(ArmingMode.AWAY));
		this.deviceRepository.save(Device.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte", null).withViolation());
		this.alarmSystemRepository.disarmOnNextFailure();
		this.alarmSystemRepository.failNextSaves(1);

		this.service.reevaluate();

		assertThat(this.alarmSystemRepository.findById(AlarmSystem.DEFAULT_SYSTEM_ID).orElseThrow().isTriggered()).isFalse();
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

	/**
	 * Simule le comportement de {@code AlarmSystemMongoRepositoryAdapter} sous
	 * collision de verrouillage optimiste : les {@code failuresRemaining} premiers
	 * appels à {@link #save(AlarmSystem)} lèvent {@link ConcurrentAlarmSystemUpdateException},
	 * comme le ferait l'adapter réel en traduisant un {@code OptimisticLockingFailureException}.
	 */
	private static final class FlakyAlarmSystemRepository implements AlarmSystemRepository {

		private final Map<String, AlarmSystem> systems = new HashMap<>();
		private int failuresRemaining;
		private int saveAttempts;
		private boolean disarmOnNextFailure;

		void failNextSaves(final int count) {
			this.failuresRemaining = count;
		}

		/**
		 * Simule un humain qui désarme le système au moment précis d'une
		 * collision : la prochaine fois qu'un {@link #save(AlarmSystem)} échoue,
		 * l'état persisté est remplacé par un système désarmé avant de lever
		 * l'exception, comme si l'écriture concurrente qui a gagné la course
		 * était ce désarmement.
		 */
		void disarmOnNextFailure() {
			this.disarmOnNextFailure = true;
		}

		/**
		 * Place l'état initial directement, sans passer par {@link #save(AlarmSystem)},
		 * pour ne pas fausser le décompte des tentatives observées par le test.
		 */
		void seed(final AlarmSystem alarmSystem) {
			this.systems.put(alarmSystem.getId(), alarmSystem);
		}

		int saveAttempts() {
			return this.saveAttempts;
		}

		@Override
		public AlarmSystem save(final AlarmSystem alarmSystem) {
			this.saveAttempts++;
			if (this.failuresRemaining > 0) {
				this.failuresRemaining--;
				if (this.disarmOnNextFailure) {
					this.disarmOnNextFailure = false;
					this.systems.put(alarmSystem.getId(), AlarmSystem.initial(alarmSystem.getId()));
				}
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

		List<AlarmStateChanged> publishedEvents() {
			return this.events;
		}
	}
}
