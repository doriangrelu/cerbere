package fr.cerbere.component.cerbere_core.adapter.in.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

/**
 * Normalisation partagée des champs texte libre saisis par l'utilisateur
 * (label de device, nom de zone...), appliquée uniquement en couche
 * présentation — voir docs/best-practices/coding-standards.md.
 */
@Mapper(componentModel = "spring")
public interface CommonTextMapper {

	@Named("normalizeFreeText")
	default String normalizeFreeText(final String value) {
		return value != null ? value.trim().toLowerCase() : null;
	}
}
