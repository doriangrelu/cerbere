package fr.cerbere.shared.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Traduit les erreurs communes à tout service Cerbère (validation des DTOs
 * entrants, corps de requête illisible, argument invalide) en {@link ProblemDetail}
 * (RFC 9457), sans connaître les exceptions métier propres à chaque module —
 * celles-ci restent gérées par un {@code @RestControllerAdvice} local à chaque
 * service. Importé explicitement via {@code @Import} sur chaque {@code *Application}
 * (pas de scan automatique : {@code fr.cerbere.shared} est hors du package de base
 * de chaque module) — voir ADR 0013.
 */
@RestControllerAdvice
public final class CommonExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(final MethodArgumentNotValidException exception) {
		final String fieldErrors = exception.getBindingResult().getFieldErrors().stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.collect(Collectors.joining("; "));
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, fieldErrors);
		problemDetail.setTitle("Validation error");
		return problemDetail;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail handleUnreadableBody(final HttpMessageNotReadableException exception) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed request body");
		problemDetail.setTitle("Bad request");
		return problemDetail;
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ProblemDetail handleIllegalArgument(final IllegalArgumentException exception) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
		problemDetail.setTitle("Bad request");
		return problemDetail;
	}
}
