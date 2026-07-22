package fr.cerbere.component.cerbere_bff.adapter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Client REST vers {@code cerbere-core}, appelé directement depuis
 * {@code cerbere-bff} sans passer par {@code api-gateway} pour cette itération
 * (recette interne, pas d'exposition internet — voir ADR 0015).
 */
@Configuration(proxyBeanMethods = false)
public final class RestClientConfig {

	@Bean
	public RestClient coreRestClient(@Value("${cerbere.core.base-url}") final String baseUrl) {
		return RestClient.builder().baseUrl(baseUrl).build();
	}

	@Bean
	public RestClient devicesMockRestClient(@Value("${cerbere.devices-mock.base-url}") final String baseUrl) {
		return RestClient.builder().baseUrl(baseUrl).build();
	}
}
