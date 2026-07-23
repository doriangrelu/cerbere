package fr.cerbere.component.cerbere_devices_mock.domain.model;

import fr.cerbere.component.cerbere_devices_mock.domain.exception.UnsupportedDeviceCommandException;

import java.util.UUID;

/**
 * Device simulé (capteur ou actionneur) appartenant au bounded context de simulation.
 * Volontairement découplé du registre officiel des devices qui vivra dans {@code cerbere-core}
 * (voir ADR 0004). Immuable : toute mutation d'état retourne une nouvelle instance.
 * {@code version} porte le numéro de version optimiste Mongo ({@code @Version} sur
 * {@code SimulatedDeviceDocument}) : {@code null} pour un device pas encore persisté,
 * préservé par toutes les méthodes {@code with*} pour que la vérification de
 * concurrence s'applique à la sauvegarde.
 */
public final class SimulatedDevice {

    private final UUID id;
    private final DeviceType type;
    private final String label;
    private final UUID zoneId;
    private final boolean autoSimulate;
    private final DeviceState currentState;
    private final Long version;

    private SimulatedDevice(final UUID id, final DeviceType type, final String label, final UUID zoneId,
                            final boolean autoSimulate, final DeviceState currentState, final Long version) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.zoneId = zoneId;
        this.autoSimulate = autoSimulate;
        this.currentState = currentState;
        this.version = version;
    }

    /**
     * Enregistre un nouveau device simulé, avec l'état initial par défaut de son type.
     */
    public static SimulatedDevice register(final UUID id, final DeviceType type, final String label, final UUID zoneId, final boolean autoSimulate) {
        return new SimulatedDevice(id, type, label, zoneId, autoSimulate, type.initialState(), null);
    }

    /**
     * Reconstruit un device simulé depuis la persistance (identifiant et état déjà connus).
     */
    public static SimulatedDevice restore(final UUID id, final DeviceType type, final String label, final UUID zoneId,
                                          final boolean autoSimulate, final DeviceState currentState, final Long version) {
        return new SimulatedDevice(id, type, label, zoneId, autoSimulate, currentState, version);
    }

    /**
     * Retourne une nouvelle instance du device avec l'état donné.
     *
     * @throws UnsupportedDeviceCommandException si l'état ne correspond pas au type du device
     */
    public SimulatedDevice withState(final DeviceState newState) {
        if (!this.type.supports(newState)) {
            throw new UnsupportedDeviceCommandException(this.id, this.type, newState);
        }
        return new SimulatedDevice(this.id, this.type, this.label, this.zoneId, this.autoSimulate, newState, this.version);
    }

    /**
     * Retourne une nouvelle instance du device avec le libellé/zone donnés,
     * suite à une modification côté registre officiel (voir ADR 0016).
     */
    public SimulatedDevice withLabelAndZone(final String newLabel, final UUID newZoneId) {
        return new SimulatedDevice(this.id, this.type, newLabel, newZoneId, this.autoSimulate, this.currentState, this.version);
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

    public boolean isAutoSimulate() {
        return this.autoSimulate;
    }

    public DeviceState getCurrentState() {
        return this.currentState;
    }

    public Long getVersion() {
        return this.version;
    }
}
