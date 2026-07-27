package fr.cerbere.component.cerbere_devices_mock.domain.model;

import fr.cerbere.component.cerbere_devices_mock.domain.exception.UnsupportedDeviceCommandException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimulatedDeviceTest {

    @Test
    void registerShouldDefaultFriendlyNameToId() {
        final UUID id = UUID.randomUUID();

        final SimulatedDevice device = SimulatedDevice.register(id, DeviceType.CONTACT, "Fenêtre cuisine", false);

        assertThat(device.getFriendlyName()).isEqualTo(id.toString());
    }

    @Test
    void withStateShouldReturnNewInstanceWithoutMutatingOriginal() {
        final SimulatedDevice device = SimulatedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Fenêtre cuisine", false);

        final SimulatedDevice updated = device.withState(ContactState.OPEN);

        assertThat(device.getCurrentState()).isEqualTo(ContactState.CLOSED);
        assertThat(updated.getCurrentState()).isEqualTo(ContactState.OPEN);
        assertThat(updated.getId()).isEqualTo(device.getId());
    }

    @Test
    void withStateShouldRejectStateFromAnotherDeviceFamily() {
        final SimulatedDevice device = SimulatedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Fenêtre cuisine", false);

        assertThatThrownBy(() -> device.withState(SirenState.ACTIVE))
                .isInstanceOf(UnsupportedDeviceCommandException.class);
    }

    @Test
    void withFriendlyNameShouldReturnNewInstanceWithoutMutatingOriginal() {
        final SimulatedDevice device = SimulatedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Fenêtre cuisine", false);

        final SimulatedDevice renamed = device.withFriendlyName("some-core-device-uuid");

        assertThat(device.getFriendlyName()).isNotEqualTo("some-core-device-uuid");
        assertThat(renamed.getFriendlyName()).isEqualTo("some-core-device-uuid");
        assertThat(renamed.getId()).isEqualTo(device.getId());
    }
}
