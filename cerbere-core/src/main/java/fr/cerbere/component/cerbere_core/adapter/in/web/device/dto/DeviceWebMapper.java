package fr.cerbere.component.cerbere_core.adapter.in.web.device.dto;

import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.infrastructure.mapper.CommonIdMapper;
import fr.cerbere.shared.dto.device.DeviceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Traduction du modèle de domaine {@link Device} vers le DTO REST partagé
 * {@link DeviceResponse} (module {@code cerbere-shared-kernel}, consommé
 * aussi par {@code cerbere-bff}).
 */
@Mapper(componentModel = "spring", uses = CommonIdMapper.class)
public interface DeviceWebMapper {

	@Mapping(target = "id", source = "id", qualifiedByName = "uuidToString")
	@Mapping(target = "zoneId", source = "zoneId", qualifiedByName = "uuidToString")
	DeviceResponse toResponse(Device device);
}
