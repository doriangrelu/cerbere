package fr.cerbere.component.cerbere_core.adapter.in.web;

import fr.cerbere.component.cerbere_core.domain.exception.AlarmNotArmedException;
import fr.cerbere.component.cerbere_core.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_core.domain.exception.ZoneNotEmptyException;
import fr.cerbere.component.cerbere_core.domain.exception.ZoneNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduit les exceptions métier du module en réponses HTTP explicites.
 */
@RestControllerAdvice
public final class CoreExceptionHandler {

	@ExceptionHandler(DeviceNotFoundException.class)
	public ResponseEntity<String> handleDeviceNotFound(final DeviceNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
	}

	@ExceptionHandler(ZoneNotFoundException.class)
	public ResponseEntity<String> handleZoneNotFound(final ZoneNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
	}

	@ExceptionHandler(ZoneNotEmptyException.class)
	public ResponseEntity<String> handleZoneNotEmpty(final ZoneNotEmptyException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
	}

	@ExceptionHandler(AlarmNotArmedException.class)
	public ResponseEntity<String> handleAlarmNotArmed(final AlarmNotArmedException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
	}
}
