package fr.cerbere.component.cerbere_bff.client.alarm;

import fr.cerbere.shared.dto.alarm.AlarmStatusResponse;
import fr.cerbere.shared.dto.alarm.ArmRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Client REST vers les endpoints {@code /api/alarm/*} de {@code cerbere-core}.
 * DTOs partagés via {@code cerbere-shared-kernel} (voir ADR 0010/0013) : pas de
 * copie locale du contrat REST.
 */
@Component
@RequiredArgsConstructor
public final class AlarmCoreClient {

	private final RestClient coreRestClient;

	public AlarmStatusResponse getStatus() {
		return this.coreRestClient.get()
			.uri("/api/alarm/status")
			.retrieve()
			.body(AlarmStatusResponse.class);
	}

	public AlarmStatusResponse arm(final String mode) {
		return this.coreRestClient.post()
			.uri("/api/alarm/arm")
			.body(new ArmRequest(mode))
			.retrieve()
			.body(AlarmStatusResponse.class);
	}

	public AlarmStatusResponse disarm() {
		return this.coreRestClient.post()
			.uri("/api/alarm/disarm")
			.retrieve()
			.body(AlarmStatusResponse.class);
	}
}
