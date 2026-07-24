package fr.cerbere.component.cerbere_history.domain.model;

/**
 * Requête de pagination indépendante du framework (le domaine ne dépend
 * jamais de {@code org.springframework.*}, voir ADR 0001) : {@code adapter/in/web}
 * traduit {@code org.springframework.data.domain.Pageable} vers/depuis ce type
 * à la frontière REST.
 */
public record PageRequest(int page, int size) {
}
