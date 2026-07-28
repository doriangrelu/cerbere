package fr.cerbere.component.cerbere_devices_bridge.domain.model;

import java.time.Instant;

/**
 * Device vu sur MQTT dont le {@code friendlyName} ne correspond à aucun
 * {@link BridgedDevice} connu : matériel Zigbee fraîchement appairé au
 * coordinateur (ou device simulé, voir ADR 0021) qui parle déjà, mais n'est pas
 * encore rattaché à un {@code Device} du registre officiel de {@code cerbere-core}.
 * C'est la matière première de l'écran Appairage (voir ADR 0023) : on ne peut
 * apparier que ce qui a déjà prouvé qu'il communique. Immuable : toute mutation
 * retourne une nouvelle instance. {@code inferredType} est déduit de la forme du
 * payload reçu ; {@code null} tant qu'aucun payload reconnaissable n'a été vu.
 */
public final class DiscoveredDevice {

	private final String friendlyName;
	private final DeviceType inferredType;
	private final Instant lastSeenAt;
	private final Long version;

	private DiscoveredDevice(final String friendlyName, final DeviceType inferredType,
							  final Instant lastSeenAt, final Long version) {
		this.friendlyName = friendlyName;
		this.inferredType = inferredType;
		this.lastSeenAt = lastSeenAt;
		this.version = version;
	}

	/**
	 * Première observation d'un device inconnu sur MQTT.
	 */
	public static DiscoveredDevice discover(final String friendlyName, final DeviceType inferredType, final Instant seenAt) {
		return new DiscoveredDevice(friendlyName, inferredType, seenAt, null);
	}

	/**
	 * Reconstruit un device découvert depuis la persistance.
	 */
	public static DiscoveredDevice restore(final String friendlyName, final DeviceType inferredType,
											final Instant lastSeenAt, final Long version) {
		return new DiscoveredDevice(friendlyName, inferredType, lastSeenAt, version);
	}

	/**
	 * Retourne une nouvelle instance rafraîchie par une observation ultérieure.
	 * Le type inféré n'est jamais écrasé par un {@code null} : un payload
	 * ultérieur non reconnaissable ne doit pas faire perdre une inférence déjà acquise.
	 */
	public DiscoveredDevice seenAgain(final DeviceType newInferredType, final Instant seenAt) {
		final DeviceType retained = newInferredType != null ? newInferredType : this.inferredType;
		return new DiscoveredDevice(this.friendlyName, retained, seenAt, this.version);
	}

	public String getFriendlyName() {
		return this.friendlyName;
	}

	public DeviceType getInferredType() {
		return this.inferredType;
	}

	public Instant getLastSeenAt() {
		return this.lastSeenAt;
	}

	public Long getVersion() {
		return this.version;
	}
}
