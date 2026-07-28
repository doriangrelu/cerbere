package fr.cerbere.component.cerbere_devices_bridge;

import fr.cerbere.shared.config.CommonJacksonConfig;
import fr.cerbere.shared.config.PermitAllSecurityConfig;
import fr.cerbere.shared.web.CommonExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Point d'entrée. Le module est principalement un worker Kafka+MQTT, mais expose
 * depuis l'ADR 0023 une petite API REST d'appairage (le Bridge est le seul canal
 * d'appairage, qu'il s'agisse de matériel réel ou du Mock) — d'où l'import des
 * configurations web communes.
 */
@SpringBootApplication
@Import({CommonJacksonConfig.class, PermitAllSecurityConfig.class, CommonExceptionHandler.class})
public class CerbereDevicesBridgeApplication {

	public static void main(final String[] args) {
		SpringApplication.run(CerbereDevicesBridgeApplication.class, args);
	}

}
