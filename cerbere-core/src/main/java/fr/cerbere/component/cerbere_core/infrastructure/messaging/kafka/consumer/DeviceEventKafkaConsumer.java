package fr.cerbere.component.cerbere_core.infrastructure.messaging.kafka.consumer;

import fr.cerbere.component.cerbere_core.domain.model.DeviceEventReport;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.HandleDeviceEventUseCase;
import fr.cerbere.shared.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consomme {@code cerbere.device.events.raw} et traduit chaque {@link EventEnvelope}
 * en {@link DeviceEventReport} pour évaluation. Les {@code eventType} non
 * reconnus sont ignorés silencieusement par {@link HandleDeviceEventUseCase}
 * lui-même (voir docs/best-practices/kafka-conventions.md), pas ici.
 */
@Component
@RequiredArgsConstructor
public final class DeviceEventKafkaConsumer {

	private final HandleDeviceEventUseCase handleDeviceEventUseCase;

	@KafkaListener(
		topics = "cerbere.device.events.raw",
		groupId = "${spring.application.name}",
		containerFactory = "eventEnvelopeKafkaListenerContainerFactory"
	)
	public void onMessage(final EventEnvelope envelope) {
		final DeviceEventReport report = new DeviceEventReport(
			envelope.deviceId(),
			envelope.eventType(),
			envelope.payload(),
			envelope.occurredAt(),
			envelope.correlationId()
		);
		this.handleDeviceEventUseCase.handle(report);
	}
}
