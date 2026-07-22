package fr.cerbere.component.cerbere_core.adapter.config;

import fr.cerbere.component.cerbere_core.application.usecase.alarm.AlarmSystemService;
import fr.cerbere.component.cerbere_core.application.usecase.alarm.HandleDeviceEventService;
import fr.cerbere.component.cerbere_core.application.usecase.device.DeleteDeviceService;
import fr.cerbere.component.cerbere_core.application.usecase.device.ListDevicesService;
import fr.cerbere.component.cerbere_core.application.usecase.device.RegisterDeviceService;
import fr.cerbere.component.cerbere_core.application.usecase.device.UpdateDeviceService;
import fr.cerbere.component.cerbere_core.application.usecase.zone.DeleteZoneService;
import fr.cerbere.component.cerbere_core.application.usecase.zone.ListZonesService;
import fr.cerbere.component.cerbere_core.application.usecase.zone.RegisterZoneService;
import fr.cerbere.component.cerbere_core.application.usecase.zone.UpdateZoneService;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.ArmSystemUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.DisarmSystemUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.GetAlarmStatusUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.alarm.HandleDeviceEventUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.device.DeleteDeviceUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.device.ListDevicesUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.device.RegisterDeviceUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.device.UpdateDeviceUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.zone.DeleteZoneUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.zone.ListZonesUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.zone.RegisterZoneUseCase;
import fr.cerbere.component.cerbere_core.domain.port.in.zone.UpdateZoneUseCase;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmStateChangedPublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlarmSystemRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.alarm.AlertPublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DevicePublisher;
import fr.cerbere.component.cerbere_core.domain.port.out.device.DeviceRepository;
import fr.cerbere.component.cerbere_core.domain.port.out.zone.ZoneRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Câblage des use-cases (couche application) contre leurs ports. Les classes de
 * {@code application.usecase} restent volontairement dépourvues d'annotations Spring
 * (voir ADR 0001) : leur instanciation et leur exposition en tant que bean se font ici.
 */
@Configuration(proxyBeanMethods = false)
public final class UseCaseConfig {

    @Bean
    public RegisterDeviceUseCase registerDeviceUseCase(final DeviceRepository deviceRepository, final DevicePublisher devicePublisher) {
        return new RegisterDeviceService(deviceRepository, devicePublisher);
    }

    @Bean
    public UpdateDeviceUseCase updateDeviceUseCase(final DeviceRepository deviceRepository) {
        return new UpdateDeviceService(deviceRepository);
    }

    @Bean
    public DeleteDeviceUseCase deleteDeviceUseCase(final DeviceRepository deviceRepository) {
        return new DeleteDeviceService(deviceRepository);
    }

    @Bean
    public ListDevicesUseCase listDevicesUseCase(final DeviceRepository deviceRepository) {
        return new ListDevicesService(deviceRepository);
    }

    @Bean
    public RegisterZoneUseCase registerZoneUseCase(final ZoneRepository zoneRepository) {
        return new RegisterZoneService(zoneRepository);
    }

    @Bean
    public UpdateZoneUseCase updateZoneUseCase(final ZoneRepository zoneRepository) {
        return new UpdateZoneService(zoneRepository);
    }

    @Bean
    public DeleteZoneUseCase deleteZoneUseCase(final ZoneRepository zoneRepository) {
        return new DeleteZoneService(zoneRepository);
    }

    @Bean
    public ListZonesUseCase listZonesUseCase(final ZoneRepository zoneRepository) {
        return new ListZonesService(zoneRepository);
    }

    @Bean
    public AlarmSystemService alarmSystemService(final AlarmSystemRepository alarmSystemRepository,
                                                 final AlarmStateChangedPublisher alarmStateChangedPublisher,
                                                 final DeviceRepository deviceRepository) {
        return new AlarmSystemService(deviceRepository, alarmSystemRepository, alarmStateChangedPublisher);
    }

    @Bean
    public ArmSystemUseCase armSystemUseCase(final AlarmSystemService alarmSystemService) {
        return alarmSystemService;
    }

    @Bean
    public DisarmSystemUseCase disarmSystemUseCase(final AlarmSystemService alarmSystemService) {
        return alarmSystemService;
    }

    @Bean
    public GetAlarmStatusUseCase getAlarmStatusUseCase(final AlarmSystemService alarmSystemService) {
        return alarmSystemService;
    }

    @Bean
    public HandleDeviceEventUseCase handleDeviceEventUseCase(final AlarmSystemRepository alarmSystemRepository,
                                                             final DeviceRepository deviceRepository,
                                                             final AlarmStateChangedPublisher alarmStateChangedPublisher,
                                                             final AlertPublisher alertPublisher) {
        return new HandleDeviceEventService(alarmSystemRepository, deviceRepository, alarmStateChangedPublisher, alertPublisher);
    }
}
