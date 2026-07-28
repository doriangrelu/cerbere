package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.event.DeviceEventOccurred;
import fr.cerbere.component.cerbere_devices_mock.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_mock.domain.exception.DeviceOfflineException;
import fr.cerbere.component.cerbere_devices_mock.domain.model.ContactState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires purs (aucun contexte Spring) du déclenchement manuel d'un
 * état sur un device simulé.
 */
class PublishDeviceEventServiceTest {

    private InMemorySimulatedDeviceRepository repository;
    private RecordingDeviceStatePublisher publisher;
    private PublishDeviceEventService service;

    @BeforeEach
    void setUp() {
        this.repository = new InMemorySimulatedDeviceRepository();
        this.publisher = new RecordingDeviceStatePublisher();
        this.service = new PublishDeviceEventService(this.repository, this.publisher);
    }

    @Test
    void triggerShouldPublishRequestedStateAndPersistIt() {
        final SimulatedDevice device = this.repository.save(SimulatedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte d'entrée"));

        final DeviceEventOccurred event = this.service.trigger(device.getId(), ContactState.OPEN.name());

        assertThat(event.newState()).isEqualTo(ContactState.OPEN);
        assertThat(event.triggeredManually()).isTrue();
        assertThat(this.publisher.publishedStates()).containsExactly(
            new PublishedState(device.getFriendlyName(), DeviceType.CONTACT, ContactState.OPEN)
        );
        assertThat(this.repository.findById(device.getId()).orElseThrow().getCurrentState()).isEqualTo(ContactState.OPEN);
    }

    @Test
    void triggerShouldThrowWhenDeviceDoesNotExist() {
        assertThatThrownBy(() -> this.service.trigger(UUID.randomUUID(), ContactState.OPEN.name()))
                .isInstanceOf(DeviceNotFoundException.class);
    }

    @Test
    void triggerShouldThrowWhenRequestedStateDoesNotBelongToDeviceType() {
        final SimulatedDevice device = this.repository.save(SimulatedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte d'entrée"));

        assertThatThrownBy(() -> this.service.trigger(device.getId(), "ACTIVE"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void triggerShouldThrowWhenDeviceIsOffline() {
        final SimulatedDevice device = this.repository.save(
            SimulatedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte d'entrée").withOnline(false)
        );

        assertThatThrownBy(() -> this.service.trigger(device.getId(), ContactState.OPEN.name()))
                .isInstanceOf(DeviceOfflineException.class);
    }

    @Test
    void triggerShouldPublishNothingWhenDeviceIsOffline() {
        final SimulatedDevice device = this.repository.save(
            SimulatedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte d'entrée").withOnline(false)
        );

        assertThatThrownBy(() -> this.service.trigger(device.getId(), ContactState.OPEN.name()))
                .isInstanceOf(DeviceOfflineException.class);

        assertThat(this.publisher.publishedStates()).isEmpty();
        assertThat(this.repository.findById(device.getId()).orElseThrow().getCurrentState()).isEqualTo(ContactState.CLOSED);
    }
}
