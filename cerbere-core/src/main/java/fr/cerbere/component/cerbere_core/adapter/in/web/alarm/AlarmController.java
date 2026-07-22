package fr.cerbere.component.cerbere_core.adapter.in.web.alarm;

import fr.cerbere.component.cerbere_core.adapter.in.web.alarm.dto.AlarmStatusResponse;
import fr.cerbere.component.cerbere_core.adapter.in.web.alarm.dto.AlarmWebMapper;
import fr.cerbere.component.cerbere_core.adapter.in.web.alarm.dto.ArmRequest;
import fr.cerbere.component.cerbere_core.domain.model.AlarmSystem;
import fr.cerbere.component.cerbere_core.domain.model.ArmingMode;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.ArmSystemUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.DisarmSystemUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.GetAlarmStatusUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de pilotage de l'alarme : armer, désarmer, consulter l'état.
 */
@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
public final class AlarmController {

	private final ArmSystemUseCase armSystemUseCase;
	private final DisarmSystemUseCase disarmSystemUseCase;
	private final GetAlarmStatusUseCase getAlarmStatusUseCase;
	private final AlarmWebMapper alarmWebMapper;

	@PostMapping("/arm")
	public AlarmStatusResponse arm(@Valid @RequestBody final ArmRequest request) {
		final ArmingMode mode = ArmingMode.valueOf(request.mode());
		final AlarmSystem alarmSystem = this.armSystemUseCase.arm(mode);
		return this.alarmWebMapper.toResponse(alarmSystem);
	}

	@PostMapping("/disarm")
	public AlarmStatusResponse disarm() {
		final AlarmSystem alarmSystem = this.disarmSystemUseCase.disarm();
		return this.alarmWebMapper.toResponse(alarmSystem);
	}

	@GetMapping("/status")
	public AlarmStatusResponse status() {
		final AlarmSystem alarmSystem = this.getAlarmStatusUseCase.getCurrentStatus();
		return this.alarmWebMapper.toResponse(alarmSystem);
	}
}
