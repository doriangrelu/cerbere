package fr.cerbere.component.cerbere_history.adapter.in.web.device.dto;

import fr.cerbere.component.cerbere_history.domain.model.DeviceEventRecord;
import fr.cerbere.component.cerbere_history.infrastructure.mapper.CommonIdMapper;
import fr.cerbere.shared.dto.history.DeviceEventHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Traduction du modèle de domaine {@link DeviceEventRecord} vers le DTO REST
 * partagé {@link DeviceEventHistoryResponse} (module {@code cerbere-shared-kernel}).
 */
@Mapper(componentModel = "spring", uses = CommonIdMapper.class)
public interface DeviceEventHistoryWebMapper {

	@Mapping(target = "eventId", source = "eventId", qualifiedByName = "uuidToString")
	@Mapping(target = "deviceId", source = "deviceId", qualifiedByName = "uuidToString")
	@Mapping(target = "zoneId", source = "zoneId", qualifiedByName = "uuidToString")
	@Mapping(target = "correlationId", source = "correlationId", qualifiedByName = "uuidToString")
	DeviceEventHistoryResponse toResponse(DeviceEventRecord record);
}
