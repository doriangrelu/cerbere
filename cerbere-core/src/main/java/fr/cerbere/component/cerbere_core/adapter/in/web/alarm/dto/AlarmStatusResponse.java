package fr.cerbere.component.cerbere_core.adapter.in.web.alarm.dto;

public record AlarmStatusResponse(
	String systemId,
	String mode,
	boolean triggered,
	String status
) {
}
