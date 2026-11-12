package gm.gov.lms.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Application roles are Keycloak realm roles with plain names ({@code EMPLOYEE}) in
 * {@code realm_access.roles}. Spring's {@code hasRole('EMPLOYEE')} expects the authority
 * {@code ROLE_EMPLOYEE}, so the converter adds the prefix. (Spring Security 7 also adds
 * {@code FACTOR_BEARER}, recording how the caller authenticated; these tests ignore it.)
 */
class KeycloakRealmRolesTests {

	private static final Map<String, Object> ACCOUNT_CLIENT_ROLES = Map.of("account", Map.of("roles",
			List.of("manage-account", "manage-account-links", "view-profile")));

	@Test
	void realmRoleBecomesRolePrefixedAuthority() {
		// shape of a real Leave-MGMT-Test token for testemployee
		var token = jwt(Map.of(
				"realm_access", Map.of("roles",
						List.of("EMPLOYEE", "offline_access", "default-roles-leave-mgmt-test", "uma_authorization")),
				"resource_access", ACCOUNT_CLIENT_ROLES));

		assertThat(rolesOf(token)).containsExactly("ROLE_EMPLOYEE");
	}

	@Test
	void userWithoutRealmRoleHasNoRoleAuthorities() {
		// shape of a real Leave-MGMT-Test token for testuser (no role assigned)
		var token = jwt(Map.of(
				"realm_access", Map.of("roles",
						List.of("offline_access", "default-roles-leave-mgmt-test", "uma_authorization")),
				"resource_access", ACCOUNT_CLIENT_ROLES));

		assertThat(rolesOf(token)).isEmpty();
	}

	@Test
	void severalRealmRolesAreAllMapped() {
		var token = jwt(Map.of("realm_access", Map.of("roles", List.of("EMPLOYEE", "SUPERVISOR"))));

		assertThat(rolesOf(token)).containsExactlyInAnyOrder("ROLE_EMPLOYEE", "ROLE_SUPERVISOR");
	}

	@Test
	void clientRolesAreIgnored() {
		// roles must be realm roles; a client role (resource_access) never grants access
		var token = jwt(Map.of("resource_access", Map.of("leave-mgmt-public", Map.of("roles", List.of("EMPLOYEE")))));

		assertThat(rolesOf(token)).isEmpty();
	}

	@Test
	void tokenWithoutRoleClaimsHasNoRoleAuthorities() {
		var token = jwt(Map.of("preferred_username", "employee.one"));

		assertThat(rolesOf(token)).isEmpty();
	}

	private static List<String> rolesOf(Jwt token) {
		return SecurityConfig.keycloakRealmRoles().convert(token).getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.filter(authority -> authority.startsWith("ROLE_"))
			.toList();
	}

	private static Jwt jwt(Map<String, Object> claims) {
		var builder = Jwt.withTokenValue("token").header("alg", "none").subject("user-1");
		claims.forEach(builder::claim);
		return builder.build();
	}

}
