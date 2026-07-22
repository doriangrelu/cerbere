package fr.cerbere.component.cerbere_core.domain.model;

/**
 * Mode d'armement demandé (entrée de {@link AlarmSystem#arm(ArmingMode)}).
 * Contrairement à {@link AlarmMode}, ne contient volontairement pas d'état
 * "désarmé" : on ne peut pas armer le système en lui demandant de le désarmer,
 * cette invalidité est écartée au niveau du type plutôt que vérifiée à l'exécution.
 *
 * <ul>
 *   <li>{@code AWAY} (mode absence) : personne dans la maison, tous les capteurs
 *   actifs (mouvement + contacts porte/fenêtre).</li>
 *   <li>{@code HOME} (mode présence/nuit) : habitants présents, les détecteurs de
 *   mouvement intérieurs sont ignorés par l'évaluation des alertes (les habitants
 *   se déplacent normalement dans la maison), seuls les contacts porte/fenêtre
 *   périphériques (et la sirène) restent surveillés.</li>
 * </ul>
 */
public enum ArmingMode {
	AWAY,
	HOME
}
