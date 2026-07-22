package fr.cerbere.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Sécurité baseline commune, temporaire : {@code permitAll} sur tout, en
 * attendant le branchement de Keycloak (phase 2, voir ADR 0007). Réservée aux
 * services internes non exposés directement sur internet. À importer
 * explicitement (@code @Import(PermitAllSecurityConfig.class)}) depuis chaque
 * application qui n'a pas encore de règles de sécurité propres ; à remplacer
 * module par module dès que Keycloak est branché.
 */
@Configuration(proxyBeanMethods = false)
public final class PermitAllSecurityConfig {

	@Bean
	public SecurityFilterChain permitAllSecurityFilterChain(final HttpSecurity httpSecurity) throws Exception {
		httpSecurity
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
		return httpSecurity.build();
	}
}
