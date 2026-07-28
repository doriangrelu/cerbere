package fr.cerbere.component.cerbere_core.domain.port.out.device;

import fr.cerbere.component.cerbere_core.domain.event.DeviceSupervisionChanged;

/**
 * Port de sortie : signale qu'un device a changé d'une façon qui peut influer
 * sur l'état d'alarme. Volontairement distinct de {@link DevicePublisher} : ce
 * signal reste interne au process (bus applicatif Spring), là où
 * {@code DevicePublisher} diffuse l'identité du device aux autres services via
 * Kafka — voir ADR 0025.
 */
public interface DeviceSupervisionChangedPublisher {

	void publish(DeviceSupervisionChanged event);
}
