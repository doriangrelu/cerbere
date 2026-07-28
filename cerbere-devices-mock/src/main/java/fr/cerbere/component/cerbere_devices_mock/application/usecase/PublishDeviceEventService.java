package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.event.DeviceEventOccurred;
import fr.cerbere.component.cerbere_devices_mock.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_mock.domain.exception.DeviceOfflineException;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.TriggerDeviceEventUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.DeviceStatePublisher;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;

import java.time.Instant;
import java.util.UUID;

/**
 * Déclenchement manuel d'un état sur un device simulé (API de contrôle du mode
 * test). L'état devient l'état courant du device, celui qu'il continuera de
 * rapporter périodiquement jusqu'au prochain déclenchement (voir
 * {@link EmitDeviceHeartbeatsService} et ADR 0024). Publié sur MQTT avec
 * exactement les mêmes payloads qu'un vrai device Zigbee2MQTT, pour que
 * {@code cerbere-devices-bridge} le traite sans distinction (voir ADR 0021).
 * Un device hors réseau refuse d'émettre : c'est précisément ce qu'on simule.
 */
public final class PublishDeviceEventService implements TriggerDeviceEventUseCase {

	private final SimulatedDeviceRepository simulatedDeviceRepository;
	private final DeviceStatePublisher deviceStatePublisher;

	public PublishDeviceEventService(final SimulatedDeviceRepository simulatedDeviceRepository,
									  final DeviceStatePublisher deviceStatePublisher) {
		this.simulatedDeviceRepository = simulatedDeviceRepository;
		this.deviceStatePublisher = deviceStatePublisher;
	}

	@Override
	public DeviceEventOccurred trigger(final UUID deviceId, final String requestedStateName) {
		final SimulatedDevice device = this.simulatedDeviceRepository.findById(deviceId)
			.orElseThrow(() -> new DeviceNotFoundException(deviceId));
		if (!device.isOnline()) {
			throw new DeviceOfflineException(deviceId);
		}
		final DeviceState requestedState = device.getType().parseState(requestedStateName);
		final SimulatedDevice updatedDevice = device.withState(requestedState);
		this.simulatedDeviceRepository.save(updatedDevice);

		this.deviceStatePublisher.publish(updatedDevice.getFriendlyName(), updatedDevice.getType(), requestedState);

		return new DeviceEventOccurred(
			UUID.randomUUID(),
			updatedDevice.getId(),
			updatedDevice.getType(),
			requestedState,
			Instant.now(),
			true
		);
	}
}
