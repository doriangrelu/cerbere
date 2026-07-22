package fr.cerbere.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Bean {@code objectMapper} commun à tous les services Cerbère, en lecture
 * tolérante (ignore les propriétés inconnues) — voir
 * docs/best-practices/kafka-conventions.md. Volontairement non {@code @Primary} :
 * le bean auto-configuré par Spring Boot reste la source par défaut, celui-ci se
 * récupère via {@code @Qualifier("objectMapper")} là où la tolérance aux champs
 * inconnus est explicitement nécessaire (ex : désérialisation d'événements Kafka
 * dont le schéma évolue indépendamment entre producteur et consommateur).
 * À importer explicitement ({@code @Import(CommonJacksonConfig.class)}) depuis
 * chaque application, le scan de composants ne traversant pas les modules.
 */
@Configuration(proxyBeanMethods = false)
public final class CommonJacksonConfig {

	@Bean
	public ObjectMapper objectMapper() {
		return JsonMapper.builder()
			.findAndAddModules()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.build();
	}
}
