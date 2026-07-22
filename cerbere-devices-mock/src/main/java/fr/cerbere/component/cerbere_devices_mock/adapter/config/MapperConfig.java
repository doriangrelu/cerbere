package fr.cerbere.component.cerbere_devices_mock.adapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Bean {@code objectMapper} additionnel en lecture tolérante (ignore les propriétés
 * inconnues) — voir docs/best-practices/kafka-conventions.md. Volontairement non
 * {@code @Primary} : le bean {@code jacksonJsonMapper} auto-configuré par Spring Boot
 * reste la source par défaut, celui-ci se récupère via {@code @Qualifier("objectMapper")}
 * là où la tolérance aux champs inconnus est explicitement nécessaire.
 */
@Configuration(proxyBeanMethods = false)
public final class MapperConfig {

	@Bean
	public ObjectMapper objectMapper() {
		return JsonMapper.builder()
			.findAndAddModules()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.build();
	}
}
