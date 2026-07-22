package fr.cerbere.component.cerbere_devices_mock.adapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Sécurité désactivée pour cette itération : module interne (simulation), jamais
 * exposé sur internet. TODO Keycloak : activer un resource server OAuth2 en phase 2
 * (voir ADR 0007).
 */
@Configuration(proxyBeanMethods = false)
public final class SecurityConfig {

	@Bean
	public SecurityFilterChain devicesMockSecurityFilterChain(final HttpSecurity httpSecurity) throws Exception {
		httpSecurity
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
		return httpSecurity.build();
	}
}
