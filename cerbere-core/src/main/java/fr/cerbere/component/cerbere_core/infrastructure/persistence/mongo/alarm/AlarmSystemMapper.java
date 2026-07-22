package fr.cerbere.component.cerbere_core.infrastructure.persistence.mongo.alarm;

import fr.cerbere.component.cerbere_core.domain.model.AlarmMode;
import fr.cerbere.component.cerbere_core.domain.model.AlarmSystem;
import org.mapstruct.Mapper;

/**
 * Traduction entre le modèle de domaine {@link AlarmSystem} et sa représentation
 * Mongo. La direction document → domaine reste une méthode par défaut :
 * {@link AlarmSystem} se reconstruit via sa fabrique statique {@code restore(...)}
 * (constructeur privé), que MapStruct ne peut pas invoquer directement.
 */
@Mapper(componentModel = "spring")
public interface AlarmSystemMapper {

	AlarmSystemDocument toDocument(AlarmSystem alarmSystem);

	default AlarmSystem toDomain(final AlarmSystemDocument document) {
		return AlarmSystem.restore(document.id(), AlarmMode.valueOf(document.mode()), document.triggered());
	}
}
