package fr.cerbere.component.cerbere_devices_mock.domain.port.in;

/**
 * Port d'entrée : faire rapporter à chaque device joignable son état courant,
 * comme le fait périodiquement un vrai capteur Zigbee (voir ADR 0024). C'est ce
 * qui entretient le {@code lastSeenAt} côté {@code cerbere-core} et permet à sa
 * supervision de vie (ADR 0020) de distinguer un device vivant d'un device muet.
 * Appelé directement par un adapter scheduler : véritable port d'entrée au sens
 * de l'ADR 0018.
 */
public interface EmitDeviceHeartbeatsUseCase {

	void emitAll();
}
