package fr.cerbere.component.cerbere_history.infrastructure.persistence.mongo.alarm;

import fr.cerbere.component.cerbere_history.domain.model.AlarmStateChangeRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Traduction entre le modèle de domaine {@link AlarmStateChangeRecord} et sa
 * représentation Mongo — voir {@code DeviceEventMapper} pour le principe
 * (record immuable, pas de fabrique nécessaire).
 */
@Mapper(componentModel = "spring")
public interface AlarmStateChangeMapper {

	@Mapping(target = "id", source = "eventId")
	AlarmStateChangeDocument toDocument(AlarmStateChangeRecord record);

	@Mapping(target = "eventId", source = "id")
	AlarmStateChangeRecord toDomain(AlarmStateChangeDocument document);
}
