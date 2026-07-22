package fr.cerbere.component.cerbere_devices_mock;

import fr.cerbere.shared.config.CommonJacksonConfig;
import fr.cerbere.shared.config.PermitAllSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Import({CommonJacksonConfig.class, PermitAllSecurityConfig.class})
public class CerbereDevicesMockApplication {

    static void main(final String[] args) {
        SpringApplication.run(CerbereDevicesMockApplication.class, args);
    }

}
