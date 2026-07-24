package fr.cerbere.component.cerbere_history.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.UUID;

/**
 * Méthodes de conversion nommées et partagées entre les mappers MapStruct du
 * module ({@code uses = CommonIdMapper.class}) — voir
 * docs/best-practices/coding-standards.md. Copie locale au module (pas dans
 * {@code cerbere-shared-kernel}) : chaque service porte sa propre infrastructure.
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
