package fr.cerbere.component.cerbere_core.domain.model;

import fr.cerbere.component.cerbere_core.domain.exception.AlarmNotArmedException;
import lombok.Getter;

/**
 * Agrégat racine du domaine alarme : mode d'armement courant et état déclenché.
 * Système mono-site — une seule instance persistée, identifiée par un
 * {@code systemId} métier stable (ex : {@code "home-1"}, voir
 * docs/best-practices/kafka-conventions.md). Immuable : toute transition
 * retourne une nouvelle instance. {@code version} porte le numéro de version
 * optimiste Mongo ({@code @Version} sur {@code AlarmSystemDocument}) : {@code null}
 * pour un système pas encore persisté, préservé par toutes les transitions pour
 * que la vérification de concurrence s'applique à la sauvegarde.
 */
@Getter
public final class AlarmSystem {

	/**
	 * Identifiant métier stable de l'unique instance persistée (système mono-site).
	 */
	public static final String DEFAULT_SYSTEM_ID = "home-1";

	private final String id;
	private final AlarmMode mode;
	private final boolean triggered;
	private final Long version;

	private AlarmSystem(final String id, final AlarmMode mode, final boolean triggered, final Long version) {
		this.id = id;
		this.mode = mode;
		this.triggered = triggered;
		this.version = version;
	}

	/**
	 * État initial d'un système fraîchement créé : désarmé, non déclenché.
	 */
	public static AlarmSystem initial(final String id) {
		return new AlarmSystem(id, AlarmMode.DISARMED, false, null);
	}

	public static AlarmSystem restore(final String id, final AlarmMode mode, final boolean triggered, final Long version) {
		return new AlarmSystem(id, mode, triggered, version);
	}

	/**
	 * Arme le système dans le mode demandé, depuis n'importe quel état (y compris
	 * déjà armé dans un autre mode, ou déclenché) ; réinitialise l'état déclenché.
	 * {@link ArmingMode} ne contenant pas de valeur "désarmé", aucune validation
	 * d'entrée n'est nécessaire ici.
	 */
	public AlarmSystem arm(final ArmingMode requestedMode) {
		final AlarmMode newMode = requestedMode == ArmingMode.AWAY ? AlarmMode.ARMED_AWAY : AlarmMode.ARMED_HOME;
		return new AlarmSystem(this.id, newMode, false, this.version);
	}

	/**
	 * Désarme le système depuis n'importe quel état. Toujours valide.
	 */
	public AlarmSystem disarm() {
		return new AlarmSystem(this.id, AlarmMode.DISARMED, false, this.version);
	}

	/**
	 * Déclenche le système suite à une violation détectée pendant qu'il est armé.
	 *
	 * @throws AlarmNotArmedException si le système est désarmé
	 */
	public AlarmSystem trigger() {
		if (this.mode == AlarmMode.DISARMED) {
			throw new AlarmNotArmedException(this.id);
		}
		return new AlarmSystem(this.id, this.mode, true, this.version);
	}

	/**
	 * Statut global dérivé, tel qu'exposé en lecture (BO, API).
	 */
	public AlarmStatus status() {
		if (this.mode == AlarmMode.DISARMED) {
			return AlarmStatus.DISARMED;
		}
		if (this.triggered) {
			return AlarmStatus.TRIGGERED;
		}
		return this.mode == AlarmMode.ARMED_AWAY ? AlarmStatus.ARMED_AWAY : AlarmStatus.ARMED_HOME;
	}
}
