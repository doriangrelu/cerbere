package fr.cerbere.component.cerbere_core.domain.model;

import lombok.Getter;

import java.util.UUID;

/**
 * Entrée du registre officiel des devices (utilisée par le BO admin et par
 * l'évaluation des alertes). Immuable : toute modification retourne une nouvelle
 * instance. Le {@link DeviceType} n'est pas modifiable après création (le type
 * physique d'un capteur ne change pas). {@code version} porte le numéro de
 * version optimiste Mongo ({@code @Version} sur {@code DeviceDocument}) : {@code null}
 * pour un device pas encore persisté, préservé par toutes les méthodes {@code with*}
 * pour que la vérification de concurrence s'applique à la sauvegarde.
 */
@Getter
public final class Device {

    private final UUID id;
    private final DeviceType type;
    private final String label;
    private final UUID zoneId;
    private final boolean violation;
    private final boolean enabled;
    private final Long version;

    private Device(final UUID id, final DeviceType type, final String label, final UUID zoneId,
                   final boolean violation, final boolean enabled, final Long version) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.zoneId = zoneId;
        this.violation = violation;
        this.enabled = enabled;
        this.version = version;
    }

    /**
     * Enregistre un nouveau device, activé par défaut. {@code id} est fourni par
     * l'appelant (pas généré) : c'est l'identifiant du device réel/simulé côté
     * bridge, qui doit correspondre au {@code deviceId} transporté dans les
     * événements Kafka pour que l'évaluation des alertes puisse le retrouver
     * (voir ADR 0004).
     */
    public static Device register(final UUID id, final DeviceType type, final String label, final UUID zoneId) {
        return new Device(id, type, label, zoneId, false, true, null);
    }

    /**
     * Reconstruit un device depuis la persistance.
     */
    public static Device restore(final UUID id, final DeviceType type, final String label, final UUID zoneId,
                                 final boolean violation, final boolean enabled, final Long version) {
        return new Device(id, type, label, zoneId, violation, enabled, version);
    }

    public Device withLabel(final String newLabel) {
        return new Device(this.id, this.type, newLabel, this.zoneId, this.violation, this.enabled, this.version);
    }

    public Device withEnabled(final boolean newEnabled) {
        return new Device(this.id, this.type, this.label, this.zoneId, this.violation, newEnabled, this.version);
    }

    public Device withZoneId(final UUID newZoneId) {
        return new Device(this.id, this.type, this.label, newZoneId, this.violation, this.enabled, this.version);
    }

    public Device withViolation() {
        return new Device(this.id, this.type, this.label, this.zoneId, true, this.enabled, this.version);
    }

    public Device withoutViolation() {
        return new Device(this.id, this.type, this.label, this.zoneId, false, this.enabled, this.version);
    }

}
