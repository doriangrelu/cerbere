package fr.cerbere.component.cerbere_core.adapter.in.web.alarm.dto;

import fr.cerbere.component.cerbere_core.domain.model.AlarmSystem;
import org.mapstruct.Mapper;

/**
 * Traduction du modèle de domaine {@link AlarmSystem} vers le DTO REST de sortie.
 * Reste une méthode par défaut : {@code status()} n'est pas un accesseur
 * JavaBean standard (pas de préfixe {@code get}/{@code is}), MapStruct ne peut
 * pas le résoudre comme source automatiquement.
 */
@Mapper(componentModel = "spring")
public interface AlarmWebMapper {

	default AlarmStatusResponse toResponse(final AlarmSystem alarmSystem) {
		return new AlarmStatusResponse(
			alarmSystem.getId(),
			alarmSystem.getMode().name(),
			alarmSystem.isTriggered(),
			alarmSystem.status().name()
		);
	}
}
