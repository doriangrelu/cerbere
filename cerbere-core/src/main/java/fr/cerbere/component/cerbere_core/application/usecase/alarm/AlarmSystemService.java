package fr.cerbere.component.cerbere_core.application.usecase.alarm;

import fr.cerbere.component.cerbere_core.application.service.AlarmTriggerReevaluationService;
import fr.cerbere.component.cerbere_core.domain.event.AlarmStateChanged;
import fr.cerbere.component.cerbere_core.domain.exception.ConcurrentAlarmSystemUpdateException;
import fr.cerbere.component.cerbere_core.domain.model.AlarmSystem;
import fr.cerbere.component.cerbere_core.domain.model.ArmingMode;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.ArmSystemUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.DisarmSystemUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.GetAlarmStatusUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmStateChangedPublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmSystemRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Implémentation des use-cases d'armement/désarmement/consultation de l'état,
 * regroupés car ils opèrent tous sur le même agrégat {@link AlarmSystem} et
 * partagent les mêmes dépendances. Le check "des devices actifs sont-ils déjà
 * en violation" est délégué à {@link AlarmTriggerReevaluationService} (partagé
 * avec la réactivation d'un device — voir ADR 0018) plutôt que dupliqué ici.
 * <p>
 * {@code AlarmSystem} est le même document mono-instance ({@code home-1}) que
 * celui protégé dans {@link AlarmTriggerReevaluationService} : un armement/
 * désarmement humain peut donc entrer en collision avec le scheduler de
 * supervision de vie ou le traitement d'un événement de device. {@link #arm}/
 * {@link #disarm} relisent l'état courant et rejouent leur décision complète
 * sur {@link ConcurrentAlarmSystemUpdateException}, avec la même borne de
 * tentatives que {@code AlarmTriggerReevaluationService}.
 */
@RequiredArgsConstructor
public final class AlarmSystemService implements ArmSystemUseCase, DisarmSystemUseCase, GetAlarmStatusUseCase {

    private static final int MAX_ATTEMPTS = 5;

    private final AlarmSystemRepository alarmSystemRepository;
    private final AlarmStateChangedPublisher alarmStateChangedPublisher;
    private final AlarmTriggerReevaluationService alarmTriggerReevaluationService;

    @Override
    public AlarmSystem arm(final ArmingMode mode) {
        return this.retrying(() -> {
            AlarmSystem current = this.findOrCreate().arm(mode);
            if (this.alarmTriggerReevaluationService.anyEnabledDeviceViolating()) {
                current = current.trigger();
            }
            return this.saveAndPublish(current, current);
        });
    }

    @Override
    public AlarmSystem disarm() {
        return this.retrying(() -> {
            final AlarmSystem current = this.findOrCreate();
            return this.saveAndPublish(current, current.disarm());
        });
    }

    private AlarmSystem retrying(final Supplier<AlarmSystem> decision) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return decision.get();
            } catch (final ConcurrentAlarmSystemUpdateException exception) {
                if (attempt == MAX_ATTEMPTS) {
                    throw exception;
                }
            }
        }
        throw new IllegalStateException("Unreachable");
    }

    @Override
    public AlarmSystem getCurrentStatus() {
        return this.findOrCreate();
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
