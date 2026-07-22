package fr.cerbere.component.cerbere_core.adapter.in.web.zone.dto;

import fr.cerbere.component.cerbere_core.domain.model.Zone;
import fr.cerbere.component.cerbere_core.infrastructure.mapper.CommonIdMapper;
import fr.cerbere.shared.dto.zone.ZoneResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Traduction du modèle de domaine {@link Zone} vers le DTO REST partagé
 * {@link ZoneResponse} (module {@code cerbere-shared-kernel}, consommé aussi
 * par {@code cerbere-bff}).
 */
@Mapper(componentModel = "spring", uses = CommonIdMapper.class)
public interface ZoneWebMapper {

	@Mapping(target = "id", source = "id", qualifiedByName = "uuidToString")
	@Mapping(target = "deviceIds", source = "deviceIds", qualifiedByName = "uuidSetToStringList")
	ZoneResponse toResponse(Zone zone);
}
