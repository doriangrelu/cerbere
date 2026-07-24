package fr.cerbere.component.cerbere_history.adapter.in.web.alarm.dto;

import fr.cerbere.component.cerbere_history.domain.model.AlertRecord;
import fr.cerbere.component.cerbere_history.infrastructure.mapper.CommonIdMapper;
import fr.cerbere.shared.dto.history.AlertHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Traduction du modèle de domaine {@link AlertRecord} vers le DTO REST
 * partagé {@link AlertHistoryResponse}.
 */
@Mapper(componentModel = "spring", uses = CommonIdMapper.class)
public interface AlertHistoryWebMapper {

	@Mapping(target = "eventId", source = "eventId", qualifiedByName = "uuidToString")
	@Mapping(target = "deviceId", source = "deviceId", qualifiedByName = "uuidToString")
	@Mapping(target = "zoneId", source = "zoneId", qualifiedByName = "uuidToString")
	@Mapping(target = "correlationId", source = "correlationId", qualifiedByName = "uuidToString")
	AlertHistoryResponse toResponse(AlertRecord record);
}
