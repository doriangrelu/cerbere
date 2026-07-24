package fr.cerbere.component.cerbere_devices_bridge.domain.port.in;

/**
 * Port d'entrée : appliquer le déclenchement/arrêt de l'alarme aux devices
 * physiques de type SIREN connus du bridge — appelé par
 * {@code AlarmStateChangedKafkaConsumer} suite à un {@code cerbere.alarm.state-changed}.
 */
public interface CommandSirenUseCase {

	void applyAlarmTriggered(boolean triggered);
}
