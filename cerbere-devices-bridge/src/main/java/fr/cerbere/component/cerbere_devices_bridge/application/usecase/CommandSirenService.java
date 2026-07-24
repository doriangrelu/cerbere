package fr.cerbere.component.cerbere_devices_bridge.application.usecase;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.BridgedDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.CommandSirenUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.BridgedDeviceRepository;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.out.DeviceCommandPublisher;

/**
 * Implémentation du use-case de pilotage des sirènes physiques suite à un
 * changement d'état de l'alarme. Recalcule à chaque appel (jamais d'état
 * intermédiaire mémorisé) : commande tous les devices SIREN connus dans le
 * sens demandé, même principe de simplicité que {@code RecomputeZoneViolationService}
 * côté `cerbere-core`.
 */
public final class CommandSirenService implements CommandSirenUseCase {

	private final BridgedDeviceRepository bridgedDeviceRepository;
	private final DeviceCommandPublisher deviceCommandPublisher;

	public CommandSirenService(final BridgedDeviceRepository bridgedDeviceRepository,
								final DeviceCommandPublisher deviceCommandPublisher) {
		this.bridgedDeviceRepository = bridgedDeviceRepository;
		this.deviceCommandPublisher = deviceCommandPublisher;
	}

	@Override
	public void applyAlarmTriggered(final boolean triggered) {
		for (final BridgedDevice siren : this.bridgedDeviceRepository.findByType(DeviceType.SIREN)) {
			if (triggered) {
				this.deviceCommandPublisher.switchOn(siren.getId());
			} else {
				this.deviceCommandPublisher.switchOff(siren.getId());
			}
		}
	}
}
