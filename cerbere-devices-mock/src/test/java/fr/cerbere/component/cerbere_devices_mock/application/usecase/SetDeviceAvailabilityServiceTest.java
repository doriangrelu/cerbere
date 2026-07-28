package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_mock.domain.model.ContactState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires purs (aucun contexte Spring) du branchement/débranchement
 * réseau d'un device simulé (voir ADR 0024).
 */
class SetDeviceAvailabilityServiceTest {

	private InMemorySimulatedDeviceRepository repository;
	private SetDeviceAvailabilityService service;

	@BeforeEach
	void setUp() {
		this.repository = new InMemorySimulatedDeviceRepository();
		this.service = new SetDeviceAvailabilityService(this.repository);
	}

	@Test
	void setOnlineShouldTakeTheDeviceOffTheNetworkWithoutLosingItsState() {
		final SimulatedDevice device = SimulatedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte");
		this.repository.save(device.withState(ContactState.OPEN));

		final SimulatedDevice offline = this.service.setOnline(device.getId(), false);

		assertThat(offline.isOnline()).isFalse();
		assertThat(offline.getCurrentState()).isEqualTo(ContactState.OPEN);
	}

	@Test
	void setOnlineShouldPutTheDeviceBackOnTheNetwork() {
		final SimulatedDevice device = SimulatedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte");
		this.repository.save(device.withOnline(false));

		final SimulatedDevice online = this.service.setOnline(device.getId(), true);

		assertThat(online.isOnline()).isTrue();
	}

	@Test
	void setOnlineShouldThrowWhenDeviceDoesNotExist() {
		assertThatThrownBy(() -> this.service.setOnline(UUID.randomUUID(), false))
			.isInstanceOf(DeviceNotFoundException.class);
	}
}
