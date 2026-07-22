package fr.cerbere.component.cerbere_bff.adapter.in.web.alarm;

import fr.cerbere.component.cerbere_bff.client.alarm.AlarmCoreClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Tableau de bord alarme : statut courant, armement AWAY/HOME, désarmement.
 * Les actions POST retournent uniquement le fragment de statut (swap htmx),
 * jamais la page entière — voir docs/best-practices/frontend-conventions.md.
 */
@Controller
@RequiredArgsConstructor
public final class AlarmDashboardController {

	private static final String STATUS_ATTRIBUTE = "alarmStatus";
	private static final String STATUS_FRAGMENT = "fragments/alarm-status :: alarmStatus";

	private final AlarmCoreClient alarmCoreClient;

	@GetMapping("/")
	public String home() {
		return "redirect:/alarm";
	}

	@GetMapping("/alarm")
	public String dashboard(final Model model) {
		model.addAttribute(STATUS_ATTRIBUTE, this.alarmCoreClient.getStatus());
		return "alarm/dashboard";
	}

	@PostMapping("/alarm/arm")
	public String arm(@RequestParam final String mode, final Model model) {
		model.addAttribute(STATUS_ATTRIBUTE, this.alarmCoreClient.arm(mode));
		return STATUS_FRAGMENT;
	}

	@PostMapping("/alarm/disarm")
	public String disarm(final Model model) {
		model.addAttribute(STATUS_ATTRIBUTE, this.alarmCoreClient.disarm());
		return STATUS_FRAGMENT;
	}
}
