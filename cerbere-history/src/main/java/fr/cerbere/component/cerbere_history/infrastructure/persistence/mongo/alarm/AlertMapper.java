package fr.cerbere.component.cerbere_history.infrastructure.persistence.mongo.alarm;

import fr.cerbere.component.cerbere_history.domain.model.AlertRecord;
import fr.cerbere.component.cerbere_history.infrastructure.mapper.CommonIdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Traduction entre le modèle de domaine {@link AlertRecord} et sa
 * représentation Mongo — voir {@code DeviceEventMapper} pour le principe
 * (record immuable, pas de fabrique nécessaire).
 */
@Mapper(componentModel = "spring", uses = CommonIdMapper.class)
public interface AlertMapper {

	@Mapping(target = "id", source = "eventId", qualifiedByName = "uuidToString")
	@Mapping(target = "deviceId", source = "deviceId", qualifiedByName = "uuidToString")
	@Mapping(target = "zoneId", source = "zoneId", qualifiedByName = "uuidToString")
	AlertDocument toDocument(AlertRecord record);

	@Mapping(target = "eventId", source = "id", qualifiedByName = "stringToUuid")
	@Mapping(target = "deviceId", source = "deviceId", qualifiedByName = "stringToUuid")
	@Mapping(target = "zoneId", source = "zoneId", qualifiedByName = "stringToUuid")
	AlertRecord toDomain(AlertDocument document);
}
