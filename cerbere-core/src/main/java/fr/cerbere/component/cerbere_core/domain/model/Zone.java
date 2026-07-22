package fr.cerbere.component.cerbere_core.domain.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Zone regroupant un ensemble de devices (ex : "Rez-de-chaussée", "Étage").
 * Immuable : toute modification retourne une nouvelle instance.
 */
public final class Zone {

	private final UUID id;
	private final String name;
	private final Set<UUID> deviceIds;

	private Zone(final UUID id, final String name, final Set<UUID> deviceIds) {
		this.id = id;
		this.name = name;
		this.deviceIds = Set.copyOf(deviceIds);
	}

	public static Zone register(final String name) {
		return new Zone(UUID.randomUUID(), name, Set.of());
	}

	public static Zone restore(final UUID id, final String name, final Set<UUID> deviceIds) {
		return new Zone(id, name, deviceIds);
	}

	public Zone withName(final String newName) {
		return new Zone(this.id, newName, this.deviceIds);
	}

	public Zone withDeviceAdded(final UUID deviceId) {
		final Set<UUID> updated = new LinkedHashSet<>(this.deviceIds);
		updated.add(deviceId);
		return new Zone(this.id, this.name, updated);
	}

	public Zone withDeviceRemoved(final UUID deviceId) {
		final Set<UUID> updated = new LinkedHashSet<>(this.deviceIds);
		updated.remove(deviceId);
		return new Zone(this.id, this.name, updated);
	}

	public UUID getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public Set<UUID> getDeviceIds() {
		return this.deviceIds;
	}
}
