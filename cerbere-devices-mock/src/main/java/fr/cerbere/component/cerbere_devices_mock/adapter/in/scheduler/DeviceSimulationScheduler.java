package fr.cerbere.component.cerbere_devices_mock.adapter.in.scheduler;

import fr.cerbere.component.cerbere_devices_mock.domain.event.DeviceEventOccurred;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.SimulateDeviceEventUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * Génère automatiquement des événements pour les devices marqués {@code autoSimulate},
 * en s'appuyant sur le même use-case ({@link SimulateDeviceEventUseCase}) que l'API de
 * déclenchement manuel — un seul chemin de publication d'événement (voir ADR 0004).
 */
@Component
public final class DeviceSimulationScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceSimulationScheduler.class);

	private final SimulatedDeviceRepository simulatedDeviceRepository;
	private final SimulateDeviceEventUseCase simulateDeviceEventUseCase;
	private final double triggerProbability;
	private final Random random;

	public DeviceSimulationScheduler(final SimulatedDeviceRepository simulatedDeviceRepository,
									  final SimulateDeviceEventUseCase simulateDeviceEventUseCase,
									  @Value("${cerbere.devices-mock.simulation.trigger-probability}") final double triggerProbability) {
		this.simulatedDeviceRepository = simulatedDeviceRepository;
		this.simulateDeviceEventUseCase = simulateDeviceEventUseCase;
		this.triggerProbability = triggerProbability;
		this.random = new Random();
	}

	@Scheduled(fixedDelayString = "${cerbere.devices-mock.simulation.fixed-delay-ms}")
	public void simulateAutoDevices() {
		final List<SimulatedDevice> autoSimulatedDevices = this.simulatedDeviceRepository.findByAutoSimulateTrue();
		autoSimulatedDevices.forEach(this::maybeSimulate);
	}

	private void maybeSimulate(final SimulatedDevice device) {
		if (this.random.nextDouble() <= this.triggerProbability) {
			final DeviceEventOccurred event = this.simulateDeviceEventUseCase.simulateRandomEvent(device.getId());
			LOGGER.debug("Simulated event {} for device {}", event.eventId(), device.getId());
		}
	}
}
