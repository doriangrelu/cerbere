package fr.cerbere.component.cerbere_devices_mock.adapter.in.web;

import fr.cerbere.component.cerbere_devices_mock.domain.exception.DeviceNotFoundException;
import fr.cerbere.component.cerbere_devices_mock.domain.exception.UnsupportedDeviceCommandException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduit les exceptions métier du module en réponses HTTP explicites.
 */
@RestControllerAdvice
public final class DevicesMockExceptionHandler {

	@ExceptionHandler(DeviceNotFoundException.class)
	public ResponseEntity<String> handleDeviceNotFound(final DeviceNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
	}

	@ExceptionHandler(UnsupportedDeviceCommandException.class)
	public ResponseEntity<String> handleUnsupportedCommand(final UnsupportedDeviceCommandException exception) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
	}
}
