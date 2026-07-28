package fr.cerbere.component.cerbere_core.application.usecase.alarm;

import fr.cerbere.component.cerbere_core.domain.event.AlertRaised;
import fr.cerbere.component.cerbere_core.domain.event.AlertSeverity;
import fr.cerbere.component.cerbere_core.domain.event.DeviceSupervisionChanged;
import fr.cerbere.component.cerbere_core.domain.model.AlarmMode;
import fr.cerbere.component.cerbere_core.domain.model.AlarmSystem;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.DeviceEventReport;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.HandleDeviceEventUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmSystemRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlertPublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceSupervisionChangedPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

/**
 * Évalue chaque événement de device rapporté : ignoré si le device est inconnu
 * du registre officiel (ex : {@code cerbere-devices-mock} pas encore aligné, ou
 * device supprimé depuis — voir ADR 0016). Sinon, l'état de violation du device
 * est mis à jour selon le mode d'armement courant (voir
 * {@link fr.cerbere.component.cerbere_core.domain.model.ArmingMode} pour la
 * différence AWAY/HOME) et une alerte est levée si la violation compte
 * réellement (système armé, device actif).
 * Marque le device {@code linked} au premier événement accepté (voir ADR 0022) :
 * c'est la seule preuve fiable qu'un device physique/simulé communique
 * effectivement sous cet id, jamais réinitialisée ensuite.
 * <p>
 * Le déclenchement de l'alarme n'est pas décidé ici : ce use-case émet
 * {@link DeviceSupervisionChanged} après avoir enregistré le device, et
 * {@code ReevaluateAlarmStateService} en dérive l'état d'alarme — même chemin
 * que pour la supervision de vie ou la réactivation d'un device (voir ADR 0025).
 */
@RequiredArgsConstructor
public final class HandleDeviceEventService implements HandleDeviceEventUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandleDeviceEventService.class);

    private static final String MOTION_EVENT_PREFIX = "device.motion";
    private static final String CONTACT_STATE_CHANGED = "device.contact.state_changed";
    private static final String MOTION_DETECTED = "device.motion.detected";

    private final AlarmSystemRepository alarmSystemRepository;
    private final DeviceRepository deviceRepository;
    private final AlertPublisher alertPublisher;
    private final DeviceSupervisionChangedPublisher supervisionChangedPublisher;

    @Override
    public void handle(final DeviceEventReport report) {
        Device device = this.deviceRepository.findById(report.deviceId()).orElse(null);
        if (device == null) {
            LOGGER.info("Ignoring event for unknown device {} (not yet synced, or deleted since)", report.deviceId());
            return;
        }

        final AlarmSystem alarmSystem = this.alarmSystemRepository.findById(AlarmSystem.DEFAULT_SYSTEM_ID)
                .orElseGet(() -> AlarmSystem.initial(AlarmSystem.DEFAULT_SYSTEM_ID));
        final boolean isViolation = this.isViolation(alarmSystem.getMode(), report);

        device = this.processDevice(isViolation, device, report.occurredAt());

        if (alarmSystem.getMode() == AlarmMode.DISARMED) {
            return;
        }

        if (!device.isEnabled()) {
            return;
        }

        if (!isViolation) {
            return;
        }

        this.raiseAlert(device, report);
    }

    private Device processDevice(final boolean isViolation, final Device device, final Instant occurredAt) {
        final Device withState = isViolation ? device.withViolation() : device.withoutViolation();
        final Device withLastSeenAt = withState.withLastSeenAt(occurredAt);
        final Device current = withLastSeenAt.isLinked() ? withLastSeenAt : withLastSeenAt.withLinked();
        final Device saved = this.deviceRepository.save(current);
        this.supervisionChangedPublisher.publish(DeviceSupervisionChanged.forDevice(saved.getId(), saved.getZoneId()));
        return saved;
    }

    private boolean isViolation(final AlarmMode mode, final DeviceEventReport report) {
        final boolean isMotionEvent = report.eventType().startsWith(MOTION_EVENT_PREFIX);
        if (mode == AlarmMode.ARMED_HOME && isMotionEvent) {
            return false;
        }
        return switch (report.eventType()) {
            case CONTACT_STATE_CHANGED -> "OPEN".equals(report.payload().get("state"));
            case MOTION_DETECTED -> Boolean.TRUE.equals(report.payload().get("detected"));
            default -> false;
        };
    }

    private void raiseAlert(final Device device, final DeviceEventReport report) {
        final AlertRaised alert = new AlertRaised(
                UUID.randomUUID(),
                device.getZoneId(),
                device.getId(),
                AlertSeverity.CRITICAL,
                "Violation detected on " + device.getLabel(),
                report.occurredAt(),
                report.correlationId()
        );
        this.alertPublisher.publish(alert);
    }

}
