package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.model.ContactState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_mock.domain.model.MotionState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires purs (aucun contexte Spring) du reporting périodique des
 * devices simulés (voir ADR 0024).
 */
class EmitDeviceHeartbeatsServiceTest {

	private InMemorySimulatedDeviceRepository repository;
	private RecordingDeviceStatePublisher publisher;
	private EmitDeviceHeartbeatsService service;

	@BeforeEach
	void setUp() {
		this.repository = new InMemorySimulatedDeviceRepository();
		this.publisher = new RecordingDeviceStatePublisher();
		this.service = new EmitDeviceHeartbeatsService(this.repository, this.publisher);
	}

	@Test
	void emitAllShouldRepublishHealthyInitialStateWhenNothingWasTriggeredYet() {
		final SimulatedDevice device = this.repository.save(SimulatedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte"));

		this.service.emitAll();

		assertThat(this.publisher.publishedStates()).containsExactly(
			new PublishedState(device.getFriendlyName(), DeviceType.CONTACT, ContactState.CLOSED)
		);
	}

	@Test
	void emitAllShouldRepublishTheLastTriggeredState() {
		final SimulatedDevice device = SimulatedDevice.register(UUID.randomUUID(), DeviceType.MOTION, "Salon");
		this.repository.save(device.withState(MotionState.DETECTED));

		this.service.emitAll();

		assertThat(this.publisher.publishedStates()).containsExactly(
			new PublishedState(device.getFriendlyName(), DeviceType.MOTION, MotionState.DETECTED)
		);
	}

	@Test
	void emitAllShouldSkipOfflineDevices() {
		this.repository.save(SimulatedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte").withOnline(false));

		this.service.emitAll();

		assertThat(this.publisher.publishedStates()).isEmpty();
	}

	@Test
	void emitAllShouldReportEveryOnlineDevice() {
		this.repository.save(SimulatedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte"));
		this.repository.save(SimulatedDevice.register(UUID.randomUUID(), DeviceType.MOTION, "Salon"));
		this.repository.save(SimulatedDevice.register(UUID.randomUUID(), DeviceType.SIREN, "Sirène").withOnline(false));

		this.service.emitAll();

		assertThat(this.publisher.publishedStates()).hasSize(2);
	}
}
