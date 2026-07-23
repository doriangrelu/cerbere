package fr.cerbere.component.cerbere_core.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.UUID;

/**
 * Méthodes de conversion nommées et partagées entre les mappers MapStruct du
 * module ({@code uses = CommonIdMapper.class}), pour éviter toute expression
 * Java inline dans une annotation {@code @Mapping} — voir
 * docs/best-practices/coding-standards.md. Doit être un {@code @Mapper} à part
 * entière (et non une simple interface utilitaire) pour que Spring en crée un
 * bean injectable dans les mappers générés qui l'utilisent via {@code uses}.
 */
@Mapper(componentModel = "spring")
public interface CommonIdMapper {

	@Named("uuidToString")
	default String uuidToString(final UUID id) {
		return id != null ? id.toString() : null;
	}

	@Named("stringToUuid")
	default UUID stringToUuid(final String id) {
		return id != null ? UUID.fromString(id) : null;
	}
}
