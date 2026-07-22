package fr.cerbere.component.cerbere_core.adapter.in.web.zone.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterZoneRequest(@NotBlank String name) {
}
