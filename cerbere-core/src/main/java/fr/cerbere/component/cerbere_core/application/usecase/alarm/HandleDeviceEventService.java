package fr.cerbere.component.cerbere_core.application.usecase.alarm;

import fr.cerbere.component.cerbere_core.domain.event.AlarmStateChanged;
import fr.cerbere.component.cerbere_core.domain.event.AlertRaised;
import fr.cerbere.component.cerbere_core.domain.event.AlertSeverity;
import fr.cerbere.component.cerbere_core.domain.model.AlarmMode;
import fr.cerbere.component.cerbere_core.domain.model.AlarmSystem;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.DeviceEventReport;
import fr.cerbere.component.cerbere_core.application.service.RecomputeZoneViolationService;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.HandleDeviceEventUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmStateChangedPublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmSystemRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlertPublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

/**
 * Évalue chaque événement de device rapporté : ignoré si le device est inconnu
 * du registre officiel (ex : {@code cerbere-devices-mock} pas encore aligné, ou
 * device supprimé depuis — voir ADR 0016), si le système est désarmé, si le
 * device est désactivé, ou si l'événement ne constitue pas une violation compte
 * tenu du mode d'armement (voir {@link fr.cerbere.component.cerbere_core.domain.model.ArmingMode}
 * pour la différence AWAY/HOME). Déclenche l'alarme et lève une alerte sinon.
 */
@RequiredArgsConstructor
public final class HandleDeviceEventService implements HandleDeviceEventUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandleDeviceEventService.class);

    private static final String MOTION_EVENT_PREFIX = "device.motion";
    private static final String CONTACT_STATE_CHANGED = "device.contact.state_changed";
    private static final String MOTION_DETECTED = "device.motion.detected";

    private final AlarmSystemRepository alarmSystemRepository;
    private final DeviceRepository deviceRepository;
    private final AlarmStateChangedPublisher alarmStateChangedPublisher;
    private final AlertPublisher alertPublisher;
    private final RecomputeZoneViolationService recomputeZoneViolationService;

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

        device = this.processDevice(isViolation, device);

        if (alarmSystem.getMode() == AlarmMode.DISARMED) {
            return;
        }

        if (!device.isEnabled()) {
            return;
        }

        if (!isViolation) {
            return;
        }

        if (!alarmSystem.isTriggered()) {
            this.triggerAlarm(alarmSystem);
        }
        this.raiseAlert(device, report);
    }

    private Device processDevice(final boolean isViolation, final Device device) {
        final Device current = isViolation ? device.withViolation() : device.withoutViolation();
        final Device saved = this.deviceRepository.save(current);
        this.recomputeZoneViolationService.recompute(saved.getZoneId());
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

    private void triggerAlarm(final AlarmSystem alarmSystem) {
        final AlarmSystem triggered = alarmSystem.trigger();
        this.alarmSystemRepository.save(triggered);
        final AlarmStateChanged event = new AlarmStateChanged(
                triggered.getId(), alarmSystem.getMode(), triggered.getMode(), true, Instant.now(), UUID.randomUUID());
        this.alarmStateChangedPublisher.publish(event);
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
