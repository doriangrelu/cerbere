package fr.cerbere.component.cerbere_devices_bridge.domain.model;

import fr.cerbere.component.cerbere_devices_bridge.domain.exception.UnsupportedDeviceStateException;

import java.util.UUID;

/**
 * Miroir local d'un device physique piloté via MQTT/Zigbee2MQTT. Volontairement
 * découplé du modèle {@code Device} du registre officiel (`cerbere-core`) — même
 * principe que {@code SimulatedDevice} dans `cerbere-devices-mock` (voir ADR 0004) :
 * la correspondance se fait uniquement via l'id transporté (ici, le
 * {@code friendly_name} Zigbee2MQTT est directement l'UUID du device, voir
 * docs/architecture/mqtt-zigbee-contracts.md). Immuable : toute mutation d'état
 * retourne une nouvelle instance. {@code version} porte le numéro de version
 * optimiste Mongo, {@code null} tant que non persisté.
 */
public final class BridgedDevice {

	private final UUID id;
	private final DeviceType type;
	private final String label;
	private final UUID zoneId;
	private final DeviceState lastKnownState;
	private final Long version;

	private BridgedDevice(final UUID id, final DeviceType type, final String label, final UUID zoneId,
						   final DeviceState lastKnownState, final Long version) {
		this.id = id;
		this.type = type;
		this.label = label;
		this.zoneId = zoneId;
		this.lastKnownState = lastKnownState;
		this.version = version;
	}

	/**
	 * Enregistre un nouveau device physique, avec l'état initial par défaut de son type.
	 */
	public static BridgedDevice register(final UUID id, final DeviceType type, final String label, final UUID zoneId) {
		return new BridgedDevice(id, type, label, zoneId, type.initialState(), null);
	}

	/**
	 * Reconstruit un device physique depuis la persistance.
	 */
	public static BridgedDevice restore(final UUID id, final DeviceType type, final String label, final UUID zoneId,
										 final DeviceState lastKnownState, final Long version) {
		return new BridgedDevice(id, type, label, zoneId, lastKnownState, version);
	}

	/**
	 * Retourne une nouvelle instance avec l'état observé donné.
	 *
	 * @throws UnsupportedDeviceStateException si l'état ne correspond pas au type du device
	 */
	public BridgedDevice withState(final DeviceState newState) {
		if (!this.type.supports(newState)) {
			throw new UnsupportedDeviceStateException(this.id, this.type, newState);
		}
		return new BridgedDevice(this.id, this.type, this.label, this.zoneId, newState, this.version);
	}

	/**
	 * Retourne une nouvelle instance avec le libellé/zone donnés, suite à une
	 * modification côté registre officiel (voir ADR 0016).
	 */
	public BridgedDevice withLabelAndZone(final String newLabel, final UUID newZoneId) {
		return new BridgedDevice(this.id, this.type, newLabel, newZoneId, this.lastKnownState, this.version);
	}

	public UUID getId() {
		return this.id;
	}

	public DeviceType getType() {
		return this.type;
	}

	public String getLabel() {
		return this.label;
	}

	public UUID getZoneId() {
		return this.zoneId;
	}

	public DeviceState getLastKnownState() {
		return this.lastKnownState;
	}

	public Long getVersion() {
		return this.version;
	}
}
