package fr.cerbere.component.cerbere_devices_mock.adapter.in.web;

import fr.cerbere.component.cerbere_devices_mock.adapter.in.web.dto.DeviceEventWebMapper;
import fr.cerbere.component.cerbere_devices_mock.domain.event.DeviceEventOccurred;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.TriggerDeviceEventUseCase;
import fr.cerbere.shared.dto.devicemock.DeviceEventResponse;
import fr.cerbere.shared.dto.devicemock.TriggerDeviceEventRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * API de contrôle pour déclencher manuellement un événement précis sur un device
 * simulé (usage démo/QA) — voir ADR 0004.
 */
@RestController
@RequestMapping("/api/devices-mock/{deviceId}/events")
public final class DeviceEventController {

	private final TriggerDeviceEventUseCase triggerDeviceEventUseCase;

	public DeviceEventController(final TriggerDeviceEventUseCase triggerDeviceEventUseCase) {
		this.triggerDeviceEventUseCase = triggerDeviceEventUseCase;
	}

	@PostMapping
	public DeviceEventResponse trigger(@PathVariable final UUID deviceId,
										@Valid @RequestBody final TriggerDeviceEventRequest request) {
		final DeviceEventOccurred event = this.triggerDeviceEventUseCase.trigger(deviceId, request.state());
		return DeviceEventWebMapper.toResponse(event);
	}
}
