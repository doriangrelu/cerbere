package fr.cerbere.component.cerbere_history.domain.model;

import java.util.List;

/**
 * Résultat paginé indépendant du framework, voir {@link PageRequest}.
 */
public record PageResult<T>(List<T> content, long totalElements, int page, int size) {
}
