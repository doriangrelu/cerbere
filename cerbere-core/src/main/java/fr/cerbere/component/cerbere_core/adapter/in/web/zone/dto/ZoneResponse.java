package fr.cerbere.component.cerbere_core.adapter.in.web.zone.dto;

import java.util.List;

public record ZoneResponse(
	String id,
	String name,
	List<String> deviceIds
) {
}
