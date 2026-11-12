package gm.gov.lms.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Stateless resource server: every request needs a Keycloak JWT, except the API docs.
 * {@code @EnableMethodSecurity} switches on {@code @PreAuthorize} on controller methods.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable()) // no cookies/sessions, so no CSRF risk
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
				.anyRequest().authenticated())
			.oauth2ResourceServer(oauth2 -> oauth2
				.jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakRealmRoles())));
		return http.build();
	}

	/**
	 * Maps Keycloak realm roles ({@code realm_access.roles}) to authorities; client roles
	 * ({@code resource_access}) are ignored. Realm roles are plain names ({@code EMPLOYEE});
	 * {@code hasRole('EMPLOYEE')} checks {@code ROLE_EMPLOYEE}, so the prefix is added here.
	 * Keycloak's built-in roles ({@code offline_access}, {@code uma_authorization},
	 * {@code default-roles-<realm>}) are not application roles and are skipped.
	 */
	static JwtAuthenticationConverter keycloakRealmRoles() {
		var converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
			if (realmAccess == null || !(realmAccess.get("roles") instanceof Collection<?> roles)) {
				return List.of();
			}
			return roles.stream()
				.map(Object::toString)
				.filter(role -> !isKeycloakDefaultRole(role))
				.<GrantedAuthority>map(role -> new SimpleGrantedAuthority("ROLE_" + role))
				.toList();
		});
		return converter;
	}

	private static boolean isKeycloakDefaultRole(String role) {
		return role.equals("offline_access") || role.equals("uma_authorization")
				|| role.startsWith("default-roles-");
	}

}
