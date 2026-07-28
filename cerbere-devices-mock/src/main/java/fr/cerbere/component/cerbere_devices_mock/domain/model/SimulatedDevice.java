package fr.cerbere.component.cerbere_devices_mock.domain.model;

import fr.cerbere.component.cerbere_devices_mock.domain.exception.UnsupportedDeviceCommandException;

import java.util.UUID;

/**
 * Device simulé (capteur ou actionneur) se comportant comme un vrai device
 * Zigbee2MQTT (voir ADR 0021) : {@code friendlyName} est l'identifiant publié
 * sur MQTT, distinct de {@code id} (clé stable, jamais exposée sur MQTT ni au
 * reste du système). Un device fraîchement créé est orphelin —
 * {@code friendlyName} vaut {@code id} par défaut, comme un device Zigbee tout
 * juste appairé — jusqu'à être renommé pour correspondre à un device du
 * registre officiel ({@code cerbere-core}), exactement comme pour du matériel
 * réel (voir docs/architecture/mqtt-zigbee-contracts.md). {@code online}
 * indique si le device est joignable sur le réseau : un device hors réseau
 * n'émet plus rien du tout (voir ADR 0024), ce qui permet d'éprouver la
 * supervision de vie de {@code cerbere-core} (ADR 0020). Immuable : toute
 * mutation retourne une nouvelle instance. {@code version} porte le numéro de
 * version optimiste Mongo ({@code @Version} sur {@code SimulatedDeviceDocument}) :
 * {@code null} pour un device pas encore persisté, préservé par toutes les
 * méthodes {@code with*} pour que la vérification de concurrence s'applique à
 * la sauvegarde.
 */
public final class SimulatedDevice {

    private final UUID id;
    private final DeviceType type;
    private final String label;
    private final String friendlyName;
    private final boolean online;
    private final DeviceState currentState;
    private final Long version;

    private SimulatedDevice(final UUID id, final DeviceType type, final String label, final String friendlyName,
                            final boolean online, final DeviceState currentState, final Long version) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.friendlyName = friendlyName;
        this.online = online;
        this.currentState = currentState;
        this.version = version;
    }

    /**
     * Enregistre un nouveau device simulé, joignable et dans l'état initial par
     * défaut de son type (l'état « tout va bien » qu'il rapportera périodiquement
     * jusqu'au premier déclenchement manuel — voir ADR 0024).
     * {@code friendlyName} démarre égal à {@code id} (device orphelin, pas encore appairé).
     */
    public static SimulatedDevice register(final UUID id, final DeviceType type, final String label) {
        return new SimulatedDevice(id, type, label, id.toString(), true, type.initialState(), null);
    }

    /**
     * Reconstruit un device simulé depuis la persistance (identifiant et état déjà connus).
     */
    public static SimulatedDevice restore(final UUID id, final DeviceType type, final String label, final String friendlyName,
                                          final boolean online, final DeviceState currentState, final Long version) {
        return new SimulatedDevice(id, type, label, friendlyName, online, currentState, version);
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
        return new SimulatedDevice(this.id, this.type, this.label, this.friendlyName, this.online, newState, this.version);
    }

    /**
     * Retourne une nouvelle instance avec le {@code friendlyName} donné — action
     * d'appairage (voir ADR 0021/0023).
     */
    public SimulatedDevice withFriendlyName(final String newFriendlyName) {
        return new SimulatedDevice(this.id, this.type, this.label, newFriendlyName, this.online, this.currentState, this.version);
    }

    /**
     * Retourne une nouvelle instance joignable ou non sur le réseau. Un device
     * hors réseau conserve son état courant : il ne le rapporte simplement plus,
     * comme un capteur dont la pile est vide ou hors de portée (voir ADR 0024).
     */
    public SimulatedDevice withOnline(final boolean newOnline) {
        return new SimulatedDevice(this.id, this.type, this.label, this.friendlyName, newOnline, this.currentState, this.version);
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

    public String getFriendlyName() {
        return this.friendlyName;
    }

    public boolean isOnline() {
        return this.online;
    }

    public DeviceState getCurrentState() {
        return this.currentState;
    }

    public Long getVersion() {
        return this.version;
    }
}
