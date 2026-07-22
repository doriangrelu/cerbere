package fr.cerbere.component.cerbere_devices_mock.domain.model;

import fr.cerbere.component.cerbere_devices_mock.domain.exception.UnsupportedDeviceCommandException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimulatedDeviceTest {

    @Test
    void withStateShouldReturnNewInstanceWithoutMutatingOriginal() {
        final SimulatedDevice device = SimulatedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Fenêtre cuisine", null, false);

        final SimulatedDevice updated = device.withState(ContactState.OPEN);

        assertThat(device.getCurrentState()).isEqualTo(ContactState.CLOSED);
        assertThat(updated.getCurrentState()).isEqualTo(ContactState.OPEN);
        assertThat(updated.getId()).isEqualTo(device.getId());
    }

    @Test
    void withStateShouldRejectStateFromAnotherDeviceFamily() {
        final SimulatedDevice device = SimulatedDevice.register(UUID.randomUUID(), DeviceType.CONTACT, "Fenêtre cuisine", null, false);

        assertThatThrownBy(() -> device.withState(SirenState.ACTIVE))
                .isInstanceOf(UnsupportedDeviceCommandException.class);
    }
}
