package fr.cerbere.component.cerbere_core.infrastructure.persistence.mongo.zone;

import fr.cerbere.component.cerbere_core.domain.model.Zone;
import fr.cerbere.component.cerbere_core.infrastructure.mapper.CommonIdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Traduction entre le modèle de domaine {@link Zone} et sa représentation Mongo.
 * La direction document → domaine reste une méthode par défaut : {@link Zone}
 * se reconstruit via sa fabrique statique {@code restore(...)} (constructeur
 * privé), que MapStruct ne peut pas invoquer directement.
 */
@Mapper(componentModel = "spring", uses = CommonIdMapper.class)
public interface ZoneMapper {

	@Mapping(target = "id", source = "id", qualifiedByName = "uuidToString")
	@Mapping(target = "deviceIds", source = "deviceIds", qualifiedByName = "uuidSetToStringList")
	ZoneDocument toDocument(Zone zone);

	default Zone toDomain(final ZoneDocument document) {
		final Set<UUID> deviceIds = document.deviceIds().stream()
			.map(UUID::fromString)
			.collect(Collectors.toSet());
		return Zone.restore(UUID.fromString(document.id()), document.name(), deviceIds, document.version());
	}
}
