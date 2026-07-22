package fr.cerbere.component.cerbere_bff.adapter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expose l'état du mode test (pilotage de {@code cerbere-devices-mock}) à
 * toutes les vues, pour que {@code fragments/nav.html} puisse conditionner
 * l'affichage de l'entrée de menu correspondante.
 */
@ControllerAdvice
public final class NavigationModelAdvice {

	@ModelAttribute("testModeEnabled")
	public boolean testModeEnabled(@Value("${cerbere.bff.test-mode.enabled:false}") final boolean testModeEnabled) {
		return testModeEnabled;
	}
}
