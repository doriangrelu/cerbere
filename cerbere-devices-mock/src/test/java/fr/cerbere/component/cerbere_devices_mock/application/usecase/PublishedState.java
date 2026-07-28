package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;

/**
 * Ce qui a effectivement été publié sur MQTT, tel que capturé par
 * {@link RecordingDeviceStatePublisher} — permet d'asserter sur le
 * {@code friendlyName} utilisé autant que sur l'état lui-même.
 */
record PublishedState(String friendlyName, DeviceType type, DeviceState state) {
}
