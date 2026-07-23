package fr.cerbere.component.cerbere_core.domain.model;

import lombok.Getter;

import java.util.UUID;

/**
 * Zone regroupant un ensemble de devices (ex : "Rez-de-chaussée", "Étage").
 * Immuable : toute modification retourne une nouvelle instance. {@code version}
 * porte le numéro de version optimiste Mongo ({@code @Version} sur {@code ZoneDocument}) :
 * {@code null} pour une zone pas encore persistée, préservé par toutes les
 * méthodes {@code with*} pour que la vérification de concurrence s'applique à
 * la sauvegarde. L'appartenance des devices ({@code Device.zoneId}) et l'état
 * {@code violation} (recalculé, jamais incrémenté — voir ADR 0017) restent des
 * questions posées à {@code DeviceRepository} par la couche application : {@code Zone}
 * ne maintient aucune collection inverse de devices.
 */
@Getter
public final class Zone {

    private final UUID id;
    private final String name;
    private final boolean violation;
    private final Long version;

    private Zone(final UUID id, final String name, final boolean violation, final Long version) {
        this.id = id;
        this.name = name;
        this.violation = violation;
        this.version = version;
    }

    public static Zone register(final String name) {
        return new Zone(UUID.randomUUID(), name, false, null);
    }

    public static Zone restore(final UUID id, final String name, final boolean violation, final Long version) {
        return new Zone(id, name, violation, version);
    }

    public Zone withName(final String newName) {
        return new Zone(this.id, newName, this.violation, this.version);
    }

    public Zone withViolation() {
        return new Zone(this.id, this.name, true, this.version);
    }

    public Zone withoutViolation() {
        return new Zone(this.id, this.name, false, this.version);
    }
}
