package fr.cerbere.component.cerbere_core;

import fr.cerbere.shared.config.CommonJacksonConfig;
import fr.cerbere.shared.config.PermitAllSecurityConfig;
import fr.cerbere.shared.web.CommonExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({CommonJacksonConfig.class, PermitAllSecurityConfig.class, CommonExceptionHandler.class})
public class CerbereCoreApplication {

	static void main(final String[] args) {
		SpringApplication.run(CerbereCoreApplication.class, args);
	}

}
