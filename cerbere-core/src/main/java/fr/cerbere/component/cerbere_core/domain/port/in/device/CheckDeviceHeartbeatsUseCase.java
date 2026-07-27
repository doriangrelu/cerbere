package fr.cerbere.component.cerbere_core.domain.port.in.device;

/**
 * Port d'entrée : évaluer la supervision de vie des devices — marque en
 * violation, et déclenche l'alarme si armé, tout device actif dont aucun
 * événement n'a été reçu depuis plus que le délai configuré (voir ADR 0020).
 * Appelé par un scheduler ({@code DeviceHeartbeatScheduler}), pas par d'autres
 * classes {@code application} : véritable port d'entrée, pas un collaborateur
 * interne (voir ADR 0018).
 */
public interface CheckDeviceHeartbeatsUseCase {

	void check();
}
