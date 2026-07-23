package fr.cerbere.component.cerbere_bff.adapter.in.web;

import fr.cerbere.component.cerbere_bff.adapter.support.ProblemDetailMessages;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

/**
 * Traduit les erreurs des appels REST vers {@code cerbere-core}/{@code cerbere-devices-mock}
 * qui ne sont pas déjà interceptées localement par un contrôleur (ex :
 * {@code AlarmDashboardController}/{@code TestModeController} ne catchent aucune
 * erreur aujourd'hui) — sans ce traducteur, une exception non gérée fait fuiter
 * des détails techniques (adresse du service, message brut de la librairie HTTP)
 * jusqu'à l'usager.
 * <p>
 * Volontairement pas un {@code @RestControllerAdvice} : ce module sert des pages
 * complètes (navigation classique) et des fragments htmx (appels Ajax) depuis
 * les mêmes contrôleurs. Ne renvoyer du {@link ProblemDetail} que pour les
 * requêtes htmx (en-tête {@code HX-Request}, consommé par le script de toast de
 * {@code layout/main.html}) ; les navigations de page complète reçoivent la
 * page d'erreur stylée habituelle plutôt qu'un JSON brut.
 * <p>
 * Ne gère volontairement que les exceptions liées à ces appels sortants — les
 * erreurs communes (validation, etc.) restent gérées par
 * {@code fr.cerbere.shared.web.CommonExceptionHandler}, et aucun handler
 * générique {@code Exception.class} n'est ajouté ici pour ne pas interférer
 * avec la résolution des pages d'erreur 404 (voir templates/error).
 */
@ControllerAdvice
@RequiredArgsConstructor
public final class BffExceptionHandler {

	private static final String HTMX_REQUEST_HEADER = "HX-Request";
	private static final String GENERIC_ERROR_VIEW = "error";
	private static final String MESSAGE_ATTRIBUTE = "message";
	private static final String STATUS_ATTRIBUTE = "status";

	private final ProblemDetailMessages problemDetailMessages;

	@ExceptionHandler(HttpStatusCodeException.class)
	public Object handleDownstreamHttpError(final HttpStatusCodeException exception, final HttpServletRequest request, final Model model) {
		final String detail = this.problemDetailMessages.extractDetail(exception);
		if (this.isHtmxRequest(request)) {
			return this.problemDetailResponse(exception.getStatusCode(), "Erreur du service appelé", detail);
		}
		return this.errorView(model, exception.getStatusCode().value(), detail);
	}

	@ExceptionHandler(ResourceAccessException.class)
	public Object handleUnreachableService(final ResourceAccessException exception, final HttpServletRequest request, final Model model) {
		final String detail = "Service temporairement indisponible.";
		if (this.isHtmxRequest(request)) {
			return this.problemDetailResponse(HttpStatus.SERVICE_UNAVAILABLE, "Service indisponible", detail);
		}
		return this.errorView(model, HttpStatus.SERVICE_UNAVAILABLE.value(), detail);
	}

	@ExceptionHandler(RestClientException.class)
	public Object handleUnexpectedRestClientError(final RestClientException exception, final HttpServletRequest request, final Model model) {
		final String detail = "Erreur de communication avec un service.";
		if (this.isHtmxRequest(request)) {
			return this.problemDetailResponse(HttpStatus.BAD_GATEWAY, "Erreur de communication", detail);
		}
		return this.errorView(model, HttpStatus.BAD_GATEWAY.value(), detail);
	}

	private ResponseEntity<ProblemDetail> problemDetailResponse(final HttpStatusCode status, final String title, final String detail) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
		problemDetail.setTitle(title);
		return ResponseEntity.status(status).body(problemDetail);
	}

	private String errorView(final Model model, final int status, final String message) {
		model.addAttribute(STATUS_ATTRIBUTE, status);
		model.addAttribute(MESSAGE_ATTRIBUTE, message);
		return GENERIC_ERROR_VIEW;
	}

	private boolean isHtmxRequest(final HttpServletRequest request) {
		return "true".equalsIgnoreCase(request.getHeader(HTMX_REQUEST_HEADER));
	}
}
