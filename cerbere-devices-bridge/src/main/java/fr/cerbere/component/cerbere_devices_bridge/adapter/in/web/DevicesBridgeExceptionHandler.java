package fr.cerbere.component.cerbere_devices_bridge.adapter.in.web;

import fr.cerbere.component.cerbere_devices_bridge.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_bridge.domain.exception.DiscoveredDeviceNotFoundException;
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
public final class DevicesBridgeExceptionHandler {

	@ExceptionHandler(DiscoveredDeviceNotFoundException.class)
	public ProblemDetail handleDiscoveredDeviceNotFound(final DiscoveredDeviceNotFoundException exception) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
		problemDetail.setTitle("Discovered device not found");
		return problemDetail;
	}

	@ExceptionHandler(DeviceNotFoundException.class)
	public ProblemDetail handleDeviceNotFound(final DeviceNotFoundException exception) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
		problemDetail.setTitle("Bridged device not found");
		return problemDetail;
	}
}
