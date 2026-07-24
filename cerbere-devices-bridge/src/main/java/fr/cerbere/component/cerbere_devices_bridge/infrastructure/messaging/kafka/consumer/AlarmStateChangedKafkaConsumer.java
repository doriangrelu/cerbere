package fr.cerbere.component.cerbere_devices_bridge.infrastructure.messaging.kafka.consumer;

import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.CommandSirenUseCase;
import fr.cerbere.shared.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consomme {@code cerbere.alarm.state-changed}, publié par {@code cerbere-core}
 * (déjà multi-consommateurs — `cerbere-history`/`cerbere-notification`, voir
 * docs/best-practices/kafka-conventions.md ; le bridge en devient un
 * consommateur indépendant de plus, aucun changement côté `cerbere-core`).
 * Pilote les sirènes physiques en fonction du champ {@code triggered}.
 */
@Component
@RequiredArgsConstructor
public final class AlarmStateChangedKafkaConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlarmStateChangedKafkaConsumer.class);

	private final CommandSirenUseCase commandSirenUseCase;

	@KafkaListener(
		topics = "cerbere.alarm.state-changed",
		groupId = "${spring.application.name}",
		containerFactory = "eventEnvelopeKafkaListenerContainerFactory"
	)
	public void onMessage(final EventEnvelope envelope) {
		try {
			final boolean triggered = envelope.getPayloadValue("triggered", Boolean.class).orElse(false);
			this.commandSirenUseCase.applyAlarmTriggered(triggered);
		} catch (final RuntimeException exception) {
			LOGGER.error("Failed to process alarm state change (eventId={}): {}", envelope.eventId(), exception.getMessage());
		}
	}
}
