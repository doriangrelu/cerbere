package fr.cerbere.component.cerbere_core.domain.port.out.device;

import fr.cerbere.component.cerbere_core.domain.event.DeviceCreated;
import fr.cerbere.component.cerbere_core.domain.event.DeviceDeleted;
import fr.cerbere.component.cerbere_core.domain.event.DeviceUpdated;

public interface DevicePublisher {

    void publish(DeviceCreated event);

    void publish(DeviceUpdated event);

    void publish(DeviceDeleted event);

}
