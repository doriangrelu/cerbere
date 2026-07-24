package fr.cerbere.component.cerbere_history.adapter.in.web.alarm.dto;

import fr.cerbere.component.cerbere_history.domain.model.AlarmStateChangeRecord;
import fr.cerbere.component.cerbere_history.infrastructure.mapper.CommonIdMapper;
import fr.cerbere.shared.dto.history.AlarmStateChangeHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Traduction du modèle de domaine {@link AlarmStateChangeRecord} vers le DTO
 * REST partagé {@link AlarmStateChangeHistoryResponse}.
 */
@Mapper(componentModel = "spring", uses = CommonIdMapper.class)
public interface AlarmStateChangeHistoryWebMapper {

	@Mapping(target = "eventId", source = "eventId", qualifiedByName = "uuidToString")
	@Mapping(target = "correlationId", source = "correlationId", qualifiedByName = "uuidToString")
	AlarmStateChangeHistoryResponse toResponse(AlarmStateChangeRecord record);
}
