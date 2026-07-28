package fr.cerbere.component.cerbere_devices_bridge.domain.port.in;

import java.util.UUID;

/**
 * Port d'entrée : apparier un device découvert à un device du registre officiel,
 * en demandant à la passerelle Zigbee2MQTT de renommer son {@code friendly_name}
 * en l'id du device officiel (voir ADR 0023). Le Bridge est le seul canal
 * d'appairage, que le device soit du matériel réel ou simulé (ADR 0021).
 */
public interface PairDiscoveredDeviceUseCase {

	void pair(String friendlyName, UUID coreDeviceId);
}
