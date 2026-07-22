package fr.cerbere.component.cerbere_core.adapter.in.web.alarm.dto;

import jakarta.validation.constraints.NotNull;

public record ArmRequest(@NotNull String mode) {
}
