package fr.cerbere.component.cerbere_core.domain.model;

import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Zone regroupant un ensemble de devices (ex : "Rez-de-chaussée", "Étage").
 * Immuable : toute modification retourne une nouvelle instance. {@code version}
 * porte le numéro de version optimiste Mongo ({@code @Version} sur {@code ZoneDocument}) :
 * {@code null} pour une zone pas encore persistée, préservé par toutes les
 * méthodes {@code with*} pour que la vérification de concurrence s'applique à
 * la sauvegarde.
 */
@Getter
public final class Zone {

	private final UUID id;
	private final String name;
	private final Set<UUID> deviceIds;
	private final Long version;

	private Zone(final UUID id, final String name, final Set<UUID> deviceIds, final Long version) {
		this.id = id;
		this.name = name;
		this.deviceIds = Set.copyOf(deviceIds);
		this.version = version;
	}

	public static Zone register(final String name) {
		return new Zone(UUID.randomUUID(), name, Set.of(), null);
	}

	public static Zone restore(final UUID id, final String name, final Set<UUID> deviceIds, final Long version) {
		return new Zone(id, name, deviceIds, version);
	}

	public Zone withName(final String newName) {
		return new Zone(this.id, newName, this.deviceIds, this.version);
	}

	public Zone withDeviceAdded(final UUID deviceId) {
		final Set<UUID> updated = new LinkedHashSet<>(this.deviceIds);
		updated.add(deviceId);
		return new Zone(this.id, this.name, updated, this.version);
	}

	public Zone withDeviceRemoved(final UUID deviceId) {
		final Set<UUID> updated = new LinkedHashSet<>(this.deviceIds);
		updated.remove(deviceId);
		return new Zone(this.id, this.name, updated, this.version);
	}
}
