package fr.cerbere.component.cerbere_history.infrastructure.persistence.mongo.device;

import fr.cerbere.component.cerbere_history.domain.model.DeviceEventRecord;
import fr.cerbere.component.cerbere_history.infrastructure.mapper.CommonIdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Traduction entre le modèle de domaine {@link DeviceEventRecord} et sa
 * représentation Mongo. Value object immuable (pas d'entité à identité/cycle
 * de vie) : contrairement à {@code Device}/{@code Zone}, MapStruct peut
 * construire les deux directions via le constructeur canonique du record.
 */
@Mapper(componentModel = "spring", uses = CommonIdMapper.class)
public interface DeviceEventMapper {

	@Mapping(target = "id", source = "eventId", qualifiedByName = "uuidToString")
	@Mapping(target = "deviceId", source = "deviceId", qualifiedByName = "uuidToString")
	@Mapping(target = "zoneId", source = "zoneId", qualifiedByName = "uuidToString")
	DeviceEventDocument toDocument(DeviceEventRecord record);

	@Mapping(target = "eventId", source = "id", qualifiedByName = "stringToUuid")
	@Mapping(target = "deviceId", source = "deviceId", qualifiedByName = "stringToUuid")
	@Mapping(target = "zoneId", source = "zoneId", qualifiedByName = "stringToUuid")
	DeviceEventRecord toDomain(DeviceEventDocument document);
}
