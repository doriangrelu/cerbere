package fr.cerbere.component.cerbere_devices_bridge;

import fr.cerbere.shared.config.CommonJacksonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Point d'entrée. Pas de couche web dans ce module (pur worker Kafka+MQTT) :
 * seul {@link CommonJacksonConfig} est importé, {@code PermitAllSecurityConfig}/
 * {@code CommonExceptionHandler} n'ont pas d'utilité sans contrôleur REST.
 */
@SpringBootApplication
@Import(CommonJacksonConfig.class)
public class CerbereDevicesBridgeApplication {

	public static void main(final String[] args) {
		SpringApplication.run(CerbereDevicesBridgeApplication.class, args);
	}

}
