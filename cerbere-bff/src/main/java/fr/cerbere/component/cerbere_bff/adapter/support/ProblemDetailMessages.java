package fr.cerbere.component.cerbere_bff.adapter.support;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import tools.jackson.databind.ObjectMapper;

/**
 * Extrait un message lisible du corps {@code ProblemDetail} (RFC 9457) renvoyé
 * par {@code cerbere-core}/{@code cerbere-devices-mock} suite à une erreur REST
 * (voir ADR 0013 : {@code fr.cerbere.shared.web.CommonExceptionHandler}), pour
 * l'afficher dans un fragment d'erreur plutôt que le JSON brut.
 */
@Component
public final class ProblemDetailMessages {

	private final ObjectMapper objectMapper;

	public ProblemDetailMessages(@Qualifier("objectMapper") final ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String extractDetail(final HttpStatusCodeException exception) {
		final String body = exception.getResponseBodyAsString();
		if (body == null || body.isBlank()) {
			return exception.getMessage();
		}
		try {
			final String detail = this.objectMapper.readTree(body).path("detail").asString();
			return (detail == null || detail.isBlank()) ? body : detail;
		} catch (final RuntimeException exceptionWhileParsing) {
			return body;
		}
	}
}
