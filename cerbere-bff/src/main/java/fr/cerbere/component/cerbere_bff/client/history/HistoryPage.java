package fr.cerbere.component.cerbere_bff.client.history;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Reflète les seuls champs utiles de {@code org.springframework.data.domain.Page}
 * renvoyé par {@code cerbere-history} (contenu + info de pagination) —
 * {@code ignoreUnknown} tolère les champs Spring Data supplémentaires
 * ({@code pageable}, {@code sort}, {@code first}, {@code last}...) sans les
 * porter dans le BFF.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record HistoryPage<T>(List<T> content, long totalElements, int totalPages, int number, int size) {
}
