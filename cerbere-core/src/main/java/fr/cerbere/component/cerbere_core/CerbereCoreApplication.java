package fr.cerbere.component.cerbere_core;

import fr.cerbere.shared.config.CommonJacksonConfig;
import fr.cerbere.shared.config.PermitAllSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({CommonJacksonConfig.class, PermitAllSecurityConfig.class})
public class CerbereCoreApplication {

	public static void main(final String[] args) {
		SpringApplication.run(CerbereCoreApplication.class, args);
	}

}
