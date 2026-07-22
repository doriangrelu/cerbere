package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.event.DeviceEventOccurred;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceState;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.SimulateDeviceEventUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.TriggerDeviceEventUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.DeviceEventPublisher;
import fr.cerbere.component.cerbere_devices_mock.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;

import java.time.Instant;
import java.util.UUID;

/**
 * Service central : que la source soit le scheduler de simulation automatique ou
 * l'API de déclenchement manuel, tout changement d'état d'un device simulé passe
 * par ce service, garantissant un format d'événement homogène (voir ADR 0004).
 * Implémente les ports {@link TriggerDeviceEventUseCase} et
 * {@link SimulateDeviceEventUseCase} (suffixe {@code Service} réservé aux
 * implémentations, {@code UseCase} réservé aux interfaces de port — voir
 * docs/best-practices/coding-standards.md).
 */
public final class PublishDeviceEventService implements TriggerDeviceEventUseCase, SimulateDeviceEventUseCase {

	private final SimulatedDeviceRepository simulatedDeviceRepository;
	private final DeviceEventPublisher deviceEventPublisher;

	public PublishDeviceEventService(final SimulatedDeviceRepository simulatedDeviceRepository,
									  final DeviceEventPublisher deviceEventPublisher) {
		this.simulatedDeviceRepository = simulatedDeviceRepository;
		this.deviceEventPublisher = deviceEventPublisher;
	}

	@Override
	public DeviceEventOccurred trigger(final UUID deviceId, final String requestedStateName) {
		final SimulatedDevice device = this.findDeviceOrThrow(deviceId);
		final DeviceState requestedState = device.getType().parseState(requestedStateName);
		return this.publishStateChange(device, requestedState, true);
	}

	@Override
	public DeviceEventOccurred simulateRandomEvent(final UUID deviceId) {
		final SimulatedDevice device = this.findDeviceOrThrow(deviceId);
		final DeviceState randomState = device.getType().randomState();
		return this.publishStateChange(device, randomState, false);
	}

	private SimulatedDevice findDeviceOrThrow(final UUID deviceId) {
		return this.simulatedDeviceRepository.findById(deviceId)
			.orElseThrow(() -> new DeviceNotFoundException(deviceId));
	}

	private DeviceEventOccurred publishStateChange(final SimulatedDevice device, final DeviceState newState, final boolean triggeredManually) {
		final SimulatedDevice updatedDevice = device.withState(newState);
		this.simulatedDeviceRepository.save(updatedDevice);

		final DeviceEventOccurred event = new DeviceEventOccurred(
			UUID.randomUUID(),
			updatedDevice.getId(),
			updatedDevice.getZoneId(),
			updatedDevice.getType(),
			newState,
			Instant.now(),
			UUID.randomUUID(),
			triggeredManually
		);

		this.deviceEventPublisher.publish(event);
		return event;
	}
}
