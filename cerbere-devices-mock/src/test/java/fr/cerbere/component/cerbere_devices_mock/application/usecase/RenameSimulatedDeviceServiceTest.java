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
 * Le renommage est l'action d'appairage (voir ADR 0021/0023) : la vraie
 * passerelle Zigbee2MQTT (ou le Mock qui en joue le rôle) retrouve le device
 * par son {@code friendlyName} courant, jamais par un id figé.
 */
class RenameSimulatedDeviceServiceTest {

	private InMemorySimulatedDeviceRepository repository;
	private RenameSimulatedDeviceService service;

	@BeforeEach
	void setUp() {
		this.repository = new InMemorySimulatedDeviceRepository();
		this.service = new RenameSimulatedDeviceService(this.repository);
	}

	@Test
	void renameShouldRetrieveTheDeviceByItsCurrentFriendlyNameAndApplyTheNewOne() {
		final UUID id = UUID.randomUUID();
		this.repository.save(SimulatedDevice.register(id, DeviceType.CONTACT, "Porte"));
		final UUID coreDeviceId = UUID.randomUUID();

		final SimulatedDevice renamed = this.service.rename(id.toString(), coreDeviceId.toString());

		assertThat(renamed.getFriendlyName()).isEqualTo(coreDeviceId.toString());
		assertThat(this.repository.findByFriendlyName(coreDeviceId.toString())).contains(renamed);
		assertThat(this.repository.findByFriendlyName(id.toString())).isEmpty();
	}

	@Test
	void renameShouldRejectAnUnknownCurrentFriendlyName() {
		assertThatThrownBy(() -> this.service.rename("unknown-friendly-name", UUID.randomUUID().toString()))
			.isInstanceOf(DeviceNotFoundException.class);
	}

	@Test
	void renameShouldPreserveIdAndCurrentStateAcrossTheRename() {
		final UUID id = UUID.randomUUID();
		final SimulatedDevice registered = this.repository.save(SimulatedDevice.register(id, DeviceType.CONTACT, "Porte"));
		final SimulatedDevice triggered = this.repository.save(registered.withState(ContactState.OPEN));

		final SimulatedDevice renamed = this.service.rename(triggered.getFriendlyName(), "core-device-id");

		assertThat(renamed.getId()).isEqualTo(id);
		assertThat(renamed.getCurrentState()).isEqualTo(ContactState.OPEN);
	}
}
