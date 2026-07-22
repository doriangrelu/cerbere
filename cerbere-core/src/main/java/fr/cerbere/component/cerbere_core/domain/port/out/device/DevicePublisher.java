package fr.cerbere.component.cerbere_core.domain.port.out.device;

import fr.cerbere.component.cerbere_core.domain.event.DeviceCreated;

public interface DevicePublisher {

    void publish(DeviceCreated event);

}
