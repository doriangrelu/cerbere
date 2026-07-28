package fr.cerbere.component.cerbere_core.domain.event;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Événement de domaine émis dès qu'un device voit changer quoi que ce soit
 * susceptible d'influer sur l'état d'alarme : sa violation, son activation, sa
 * zone de rattachement, ou sa disparition du registre. C'est le seul signal qui
 * déclenche le recalcul de la violation de zone et la réévaluation du
 * déclenchement — voir ADR 0025.
 * <p>
 * Contrairement aux autres événements de {@code domain.event}, celui-ci ne quitte
 * jamais le process : il transite par le bus applicatif Spring, jamais par Kafka.
 * Aucun autre service n'en dépend, il n'a donc ni {@code eventId} ni
 * {@code occurredAt} — c'est un signal de cohérence interne, pas un fait
 * historisable.
 * <p>
 * {@code affectedZoneIds} porte toutes les zones dont la violation est à
 * recalculer : celle du device, et l'ancienne en plus s'il vient d'en changer.
 * Les identifiants nuls (device sans zone) sont écartés à la construction, pour
 * qu'aucun consommateur n'ait à s'en préoccuper.
 *
 * @param deviceId        device à l'origine du changement
 * @param affectedZoneIds zones dont la violation est à recalculer, jamais nulle ni contenant {@code null}
 */
public record DeviceSupervisionChanged(
	UUID deviceId,
	Set<UUID> affectedZoneIds
) {

	public DeviceSupervisionChanged {
		affectedZoneIds = affectedZoneIds == null
			? Set.of()
			: affectedZoneIds.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * Fabrique tolérante aux zones nulles ou répétées, pour que les appelants
	 * n'aient à filtrer ni le cas « device sans zone » ni le cas « la zone n'a
	 * pas changé ».
	 *
	 * @param deviceId device à l'origine du changement
	 * @param zoneIds  zones concernées, {@code null} accepté et ignoré
	 * @return l'événement correspondant
	 */
	public static DeviceSupervisionChanged forDevice(final UUID deviceId, final UUID... zoneIds) {
		return new DeviceSupervisionChanged(deviceId, new HashSet<>(Arrays.asList(zoneIds)));
	}
}
