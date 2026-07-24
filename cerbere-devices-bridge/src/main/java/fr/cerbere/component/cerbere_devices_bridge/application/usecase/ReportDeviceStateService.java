package fr.cerbere.component.cerbere_devices_bridge.application.usecase;

import fr.cerbere.component.cerbere_devices_bridge.domain.event.DeviceEventOccurred;
import fr.cerbere.component.cerbere_devices_bridge.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.BridgedDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceState;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.ReportDeviceStateUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.BridgedDeviceRepository;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DeviceEventPublisher;

import java.time.Instant;
import java.util.UUID;

/**
 * Implémentation du use-case central : chaque état réellement observé sur un
 * device physique (reçu via MQTT) passe par ce service, garantissant un format
 * d'événement homogène — même rôle que {@code PublishDeviceEventService} côté
 * `cerbere-devices-mock`, sans la dichotomie manuel/simulé qui n'a pas de sens
 * pour du matériel réel.
 */
public final class ReportDeviceStateService implements ReportDeviceStateUseCase {

	private final BridgedDeviceRepository bridgedDeviceRepository;
	private final DeviceEventPublisher deviceEventPublisher;

	public ReportDeviceStateService(final BridgedDeviceRepository bridgedDeviceRepository,
									 final DeviceEventPublisher deviceEventPublisher) {
		this.bridgedDeviceRepository = bridgedDeviceRepository;
		this.deviceEventPublisher = deviceEventPublisher;
	}

	@Override
	public DeviceEventOccurred report(final UUID deviceId, final DeviceState observedState) {
		final BridgedDevice device = this.bridgedDeviceRepository.findById(deviceId)
			.orElseThrow(() -> new DeviceNotFoundException(deviceId));
		final BridgedDevice updated = device.withState(observedState);
		this.bridgedDeviceRepository.save(updated);

		final DeviceEventOccurred event = new DeviceEventOccurred(
			UUID.randomUUID(),
			updated.getId(),
			updated.getZoneId(),
			updated.getType(),
			observedState,
			Instant.now(),
			UUID.randomUUID()
		);
		this.deviceEventPublisher.publish(event);
		return event;
	}
}
