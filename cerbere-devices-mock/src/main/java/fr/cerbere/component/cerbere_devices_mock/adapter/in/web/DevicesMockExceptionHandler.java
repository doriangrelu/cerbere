package fr.cerbere.component.cerbere_devices_mock.adapter.in.web;

import fr.cerbere.component.cerbere_devices_mock.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_mock.domain.exception.UnsupportedDeviceCommandException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduit les exceptions métier propres à ce module en {@link ProblemDetail}.
 * Les erreurs communes (validation des DTOs, corps illisible) sont gérées par
 * {@code fr.cerbere.shared.web.CommonExceptionHandler} — voir ADR 0013.
 */
@RestControllerAdvice
public final class DevicesMockExceptionHandler {

	@ExceptionHandler(DeviceNotFoundException.class)
	public ProblemDetail handleDeviceNotFound(final DeviceNotFoundException exception) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
		problemDetail.setTitle("Simulated device not found");
		return problemDetail;
	}

	@ExceptionHandler(UnsupportedDeviceCommandException.class)
	public ProblemDetail handleUnsupportedCommand(final UnsupportedDeviceCommandException exception) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
		problemDetail.setTitle("Unsupported device command");
		return problemDetail;
	}
}
