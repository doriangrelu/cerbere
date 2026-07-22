package fr.cerbere.component.cerbere_devices_mock.adapter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Client REST vers {@code cerbere-core}, utilisé pour résoudre le type d'un
 * device existant avant de le simuler — voir ADR 0016.
 */
@Configuration(proxyBeanMethods = false)
public final class RestClientConfig {

	@Bean
	public RestClient coreRestClient(@Value("${cerbere.core.base-url}") final String baseUrl) {
		return RestClient.builder().baseUrl(baseUrl).build();
	}
}
