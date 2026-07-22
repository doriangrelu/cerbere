package fr.cerbere.component.cerbere_bff.client.zone;

import fr.cerbere.shared.dto.zone.RegisterZoneRequest;
import fr.cerbere.shared.dto.zone.UpdateZoneRequest;
import fr.cerbere.shared.dto.zone.ZoneResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Client REST vers les endpoints {@code /api/zones/*} de {@code cerbere-core}.
 * DTOs partagés via {@code cerbere-shared-kernel} (voir ADR 0010/0013) : pas de
 * copie locale du contrat REST.
 */
@Component
@RequiredArgsConstructor
public final class ZoneCoreClient {

	private final RestClient coreRestClient;

	public List<ZoneResponse> listAll() {
		return this.coreRestClient.get()
			.uri("/api/zones")
			.retrieve()
			.body(new ParameterizedTypeReference<List<ZoneResponse>>() {
			});
	}

	public ZoneResponse register(final RegisterZoneRequest request) {
		return this.coreRestClient.post()
			.uri("/api/zones")
			.body(request)
			.retrieve()
			.body(ZoneResponse.class);
	}

	public ZoneResponse update(final String id, final UpdateZoneRequest request) {
		return this.coreRestClient.put()
			.uri("/api/zones/{id}", id)
			.body(request)
			.retrieve()
			.body(ZoneResponse.class);
	}

	public void delete(final String id) {
		this.coreRestClient.delete()
			.uri("/api/zones/{id}", id)
			.retrieve()
			.toBodilessEntity();
	}
}
