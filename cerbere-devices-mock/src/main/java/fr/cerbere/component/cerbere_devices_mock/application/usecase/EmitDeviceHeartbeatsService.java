package fr.cerbere.component.cerbere_devices_mock.application.usecase;

import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.EmitDeviceHeartbeatsUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.DeviceStatePublisher;
import fr.cerbere.component.cerbere_devices_mock.domain.port.out.SimulatedDeviceRepository;

/**
 * Fait rapporter périodiquement à chaque device joignable son état courant,
 * comme le fait un vrai capteur Zigbee (voir ADR 0024) : le dernier état
 * déclenché manuellement, ou l'état initial « tout va bien » de son type tant
 * qu'aucun déclenchement n'a eu lieu. Aucun état n'est inventé ni tiré au
 * hasard — le device se contente de répéter ce qu'il observe, ce qui rend le
 * flux prévisible et entretient le {@code lastSeenAt} de {@code cerbere-core}.
 * Un device hors réseau est purement et simplement ignoré : plus aucune
 * émission, c'est ce qui doit finir par le faire marquer injoignable (ADR 0020).
 */
public final class EmitDeviceHeartbeatsService implements EmitDeviceHeartbeatsUseCase {

	private final SimulatedDeviceRepository simulatedDeviceRepository;
	private final DeviceStatePublisher deviceStatePublisher;

	public EmitDeviceHeartbeatsService(final SimulatedDeviceRepository simulatedDeviceRepository,
										final DeviceStatePublisher deviceStatePublisher) {
		this.simulatedDeviceRepository = simulatedDeviceRepository;
		this.deviceStatePublisher = deviceStatePublisher;
	}

	@Override
	public void emitAll() {
		for (final SimulatedDevice device : this.simulatedDeviceRepository.findByOnlineTrue()) {
			this.deviceStatePublisher.publish(device.getFriendlyName(), device.getType(), device.getCurrentState());
		}
	}
}
