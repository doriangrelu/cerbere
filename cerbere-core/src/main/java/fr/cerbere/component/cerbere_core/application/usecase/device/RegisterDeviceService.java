package fr.cerbere.component.cerbere_core.application.usecase.device;

import fr.cerbere.component.cerbere_core.domain.event.DeviceCreated;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.DeviceType;
import fr.cerbere.component.cerbere_core.domain.port.in.device.RegisterDeviceUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DevicePublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Implémentation du use-case d'enregistrement d'un device.
 */
@RequiredArgsConstructor
public class RegisterDeviceService implements RegisterDeviceUseCase {

    private final DeviceRepository deviceRepository;
    private final DevicePublisher publisher;

    @Override
    public Device register(final UUID id, final DeviceType type, final String label, final UUID zoneId) {
        final Device device = Device.register(id, type, label, zoneId);
        final Device created = this.deviceRepository.save(device);
        final DeviceCreated deviceCreated = new DeviceCreated(
                created.getId(),
                created.getLabel(),
                created.getType(),
                created.getZoneId(),
                Instant.now(),
                UUID.randomUUID()
        );
        this.publisher.publish(deviceCreated);
        return created;
    }
}
