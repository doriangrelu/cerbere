package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.event.DeviceEventOccurred;
import fr.cerbere.component.cerbere_devices_mock.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_mock.domain.model.ContactState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.DeviceEventPublisher;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;
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
 * Tests unitaires purs (aucun contexte Spring) du service central de publication
 * d'événements, exercé aussi bien par le chemin manuel que par le chemin simulé.
 */
class PublishDeviceEventServiceTest {

    private InMemorySimulatedDeviceRepository repository;
    private RecordingDeviceEventPublisher publisher;
    private PublishDeviceEventService service;

    @BeforeEach
    void setUp() {
        this.repository = new InMemorySimulatedDeviceRepository();
        this.publisher = new RecordingDeviceEventPublisher();
        this.service = new PublishDeviceEventService(this.repository, this.publisher);
    }

    @Test
    void triggerShouldPublishRequestedStateAndPersistIt() {
        final SimulatedDevice device = this.repository.save(SimulatedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte d'entrée", null, false));

        final DeviceEventOccurred event = this.service.trigger(device.getId(), ContactState.OPEN.name());

        assertThat(event.newState()).isEqualTo(ContactState.OPEN);
        assertThat(event.triggeredManually()).isTrue();
        assertThat(this.publisher.publishedEvents()).containsExactly(event);
        assertThat(this.repository.findById(device.getId()).orElseThrow().getCurrentState()).isEqualTo(ContactState.OPEN);
    }

    @Test
    void triggerShouldThrowWhenDeviceDoesNotExist() {
        assertThatThrownBy(() -> this.service.trigger(UUID.randomUUID(), ContactState.OPEN.name()))
                .isInstanceOf(DeviceNotFoundException.class);
    }

    @Test
    void triggerShouldThrowWhenRequestedStateDoesNotBelongToDeviceType() {
        final SimulatedDevice device = this.repository.save(SimulatedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Porte d'entrée", null, false));

        assertThatThrownBy(() -> this.service.trigger(device.getId(), "ACTIVE"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void simulateRandomEventShouldMarkEventAsNotManual() {
        final SimulatedDevice device = this.repository.save(SimulatedDevice.register(UUID.randomUUID(), DeviceType.MOTION, "Salon", null, true));

        final DeviceEventOccurred event = this.service.simulateRandomEvent(device.getId());

        assertThat(event.triggeredManually()).isFalse();
        assertThat(this.publisher.publishedEvents()).containsExactly(event);
    }

    private static final class InMemorySimulatedDeviceRepository implements SimulatedDeviceRepository {

        private final Map<UUID, SimulatedDevice> devices = new HashMap<>();

        @Override
        public SimulatedDevice save(final SimulatedDevice device) {
            this.devices.put(device.getId(), device);
            return device;
        }

        @Override
        public Optional<SimulatedDevice> findById(final UUID id) {
            return Optional.ofNullable(this.devices.get(id));
        }

        @Override
        public List<SimulatedDevice> findAll() {
            return List.copyOf(this.devices.values());
        }

        @Override
        public List<SimulatedDevice> findByAutoSimulateTrue() {
            return this.devices.values().stream().filter(SimulatedDevice::isAutoSimulate).toList();
        }
    }

    private static final class RecordingDeviceEventPublisher implements DeviceEventPublisher {

        private final List<DeviceEventOccurred> events = new ArrayList<>();

        @Override
        public void publish(final DeviceEventOccurred event) {
            this.events.add(event);
        }

        List<DeviceEventOccurred> publishedEvents() {
            return this.events;
        }
    }
}
