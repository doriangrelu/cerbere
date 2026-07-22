package fr.cerbere.component.cerbere_core.adapter.in.web.zone;

import fr.cerbere.component.cerbere_core.adapter.in.web.mapper.CommonTextMapper;
import fr.cerbere.component.cerbere_core.adapter.in.web.zone.dto.RegisterZoneRequest;
import fr.cerbere.component.cerbere_core.adapter.in.web.zone.dto.UpdateZoneRequest;
import fr.cerbere.component.cerbere_core.adapter.in.web.zone.dto.ZoneResponse;
import fr.cerbere.component.cerbere_core.adapter.in.web.zone.dto.ZoneWebMapper;
import fr.cerbere.component.cerbere_core.domain.model.Zone;
import fr.cerbere.component.cerbere_core.domain.port.in.zone.DeleteZoneUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.zone.ListZonesUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.zone.RegisterZoneUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.zone.UpdateZoneUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * API REST de gestion des zones (CRUD).
 */
@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public final class ZoneController {

	private final RegisterZoneUseCase registerZoneUseCase;
	private final UpdateZoneUseCase updateZoneUseCase;
	private final DeleteZoneUseCase deleteZoneUseCase;
	private final ListZonesUseCase listZonesUseCase;
	private final ZoneWebMapper zoneWebMapper;
	private final CommonTextMapper commonTextMapper;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ZoneResponse register(@Valid @RequestBody final RegisterZoneRequest request) {
		final String name = this.commonTextMapper.normalizeFreeText(request.name());
		final Zone zone = this.registerZoneUseCase.register(name);
		return this.zoneWebMapper.toResponse(zone);
	}

	@GetMapping
	public List<ZoneResponse> listAll() {
		return this.listZonesUseCase.listAll().stream()
			.map(this.zoneWebMapper::toResponse)
			.toList();
	}

	@PutMapping("/{id}")
	public ZoneResponse update(@PathVariable final UUID id, @Valid @RequestBody final UpdateZoneRequest request) {
		final String name = this.commonTextMapper.normalizeFreeText(request.name());
		final Zone zone = this.updateZoneUseCase.update(id, name);
		return this.zoneWebMapper.toResponse(zone);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable final UUID id) {
		this.deleteZoneUseCase.delete(id);
	}
}
