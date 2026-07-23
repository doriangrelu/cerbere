package fr.cerbere.component.cerbere_core.adapter.in.web;

import fr.cerbere.component.cerbere_core.domain.exception.AlarmNotArmedException;
import fr.cerbere.component.cerbere_core.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_core.domain.exception.DuplicateDeviceLabelException;
import fr.cerbere.component.cerbere_core.domain.exception.ZoneNotEmptyException;
import fr.cerbere.component.cerbere_core.domain.exception.ZoneNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduit les exceptions métier propres à ce module en {@link ProblemDetail}
 * (RFC 9457). Les erreurs communes (validation des DTOs, corps illisible) sont
 * gérées par {@code fr.cerbere.shared.web.CommonExceptionHandler} — voir ADR 0013.
 */
@RestControllerAdvice
public final class CoreExceptionHandler {

	@ExceptionHandler(DeviceNotFoundException.class)
	public ProblemDetail handleDeviceNotFound(final DeviceNotFoundException exception) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
		problemDetail.setTitle("Device not found");
		return problemDetail;
	}

	@ExceptionHandler(ZoneNotFoundException.class)
	public ProblemDetail handleZoneNotFound(final ZoneNotFoundException exception) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
		problemDetail.setTitle("Zone not found");
		return problemDetail;
	}

	@ExceptionHandler(ZoneNotEmptyException.class)
	public ProblemDetail handleZoneNotEmpty(final ZoneNotEmptyException exception) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
		problemDetail.setTitle("Zone not empty");
		return problemDetail;
	}

	@ExceptionHandler(AlarmNotArmedException.class)
	public ProblemDetail handleAlarmNotArmed(final AlarmNotArmedException exception) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
		problemDetail.setTitle("Alarm not armed");
		return problemDetail;
	}

	@ExceptionHandler(DuplicateDeviceLabelException.class)
	public ProblemDetail handleDuplicateDeviceLabel(final DuplicateDeviceLabelException exception) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
		problemDetail.setTitle("Duplicate device label");
		return problemDetail;
	}
}
