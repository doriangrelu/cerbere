package fr.cerbere.component.cerbere_core.application.usecase.alarm;

import fr.cerbere.component.cerbere_core.domain.event.AlarmStateChanged;
import fr.cerbere.component.cerbere_core.domain.model.AlarmSystem;
import fr.cerbere.component.cerbere_core.domain.model.ArmingMode;
import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.AlarmMode;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.ArmSystemUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.DisarmSystemUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.GetAlarmStatusUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.ReevaluateAlarmTriggerUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmStateChangedPublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmSystemRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Implémentation des use-cases d'armement/désarmement/consultation de l'état,
 * regroupés car ils opèrent tous sur le même agrégat {@link AlarmSystem} et
 * partagent les mêmes dépendances.
 */
@RequiredArgsConstructor
public final class AlarmSystemService implements ArmSystemUseCase, DisarmSystemUseCase, GetAlarmStatusUseCase, ReevaluateAlarmTriggerUseCase {

    private final DeviceRepository deviceRepository;
    private final AlarmSystemRepository alarmSystemRepository;
    private final AlarmStateChangedPublisher alarmStateChangedPublisher;

    @Override
    public AlarmSystem arm(final ArmingMode mode) {
        AlarmSystem current = this.findOrCreate()
                .arm(mode);
        if (this.anyEnabledDeviceViolating()) {
            current = current.trigger();
        }
        return this.saveAndPublish(current, current);
    }

    @Override
    public AlarmSystem disarm() {
        final AlarmSystem current = this.findOrCreate();
        return this.saveAndPublish(current, current.disarm());
    }

    @Override
    public AlarmSystem getCurrentStatus() {
        return this.findOrCreate();
    }

    /**
     * Réévalue le système sans changer son mode : couvre le cas d'un device
     * réactivé (passage inactif → actif) alors qu'il était déjà en violation
     * pendant son inactivité (ex : contact resté ouvert) — l'armement initial
     * ne pouvait pas le savoir puisque ce device était filtré comme désactivé.
     */
    @Override
    public void reevaluate() {
        final AlarmSystem current = this.findOrCreate();
        if (current.getMode() == AlarmMode.DISARMED || current.isTriggered()) {
            return;
        }
        if (this.anyEnabledDeviceViolating()) {
            this.saveAndPublish(current, current.trigger());
        }
    }

    private boolean anyEnabledDeviceViolating() {
        return this.deviceRepository.findAll()
                .stream()
                .filter(Device::isEnabled)
                .anyMatch(Device::isViolation);
    }

    private AlarmSystem findOrCreate() {
        return this.alarmSystemRepository.findById(AlarmSystem.DEFAULT_SYSTEM_ID)
                .orElseGet(() -> AlarmSystem.initial(AlarmSystem.DEFAULT_SYSTEM_ID));
    }

    private AlarmSystem saveAndPublish(final AlarmSystem previous, final AlarmSystem updated) {
        final AlarmSystem saved = this.alarmSystemRepository.save(updated);
        final AlarmStateChanged event = new AlarmStateChanged(
                saved.getId(), previous.getMode(), saved.getMode(), saved.isTriggered(), Instant.now(), UUID.randomUUID());
        this.alarmStateChangedPublisher.publish(event);
        return saved;
    }
}
