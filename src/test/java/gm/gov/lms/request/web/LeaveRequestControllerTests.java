package gm.gov.lms.request.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gm.gov.lms.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The dummy API the presenter calls from Postman: proves the Keycloak token reaches the
 * API and that roles decide access (401 no token, 403 wrong role, 200 right role).
 */
@WebMvcTest(LeaveRequestController.class)
@Import(SecurityConfig.class)
class LeaveRequestControllerTests {

	@Autowired
	MockMvc mvc;

	@MockitoBean
	JwtDecoder jwtDecoder; // tokens come from jwt() below, never from Keycloak

	@Test
	void whoAmIWithoutTokenIsUnauthorized() throws Exception {
		mvc.perform(get("/api/v1/leaves/whoami"))
			.andExpect(status().isUnauthorized())
			.andExpect(header().string("X-API-Version", "0.0.1")); // even on errors
	}

	@Test
	void whoAmIReturnsUsernameAndRealmRolesFromToken() throws Exception {
		mvc.perform(get("/api/v1/leaves/whoami")
				.with(jwt().jwt(token -> token.claim("preferred_username", "employee.one"))
					.authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE"),
							new SimpleGrantedAuthority("FACTOR_BEARER")))) // added by Spring Security 7
			.andExpect(status().isOk())
			.andExpect(header().string("X-API-Version", "0.0.1"))
			.andExpect(jsonPath("$.username").value("employee.one"))
			.andExpect(jsonPath("$.roles.length()").value(1))
			.andExpect(jsonPath("$.roles[0]").value("EMPLOYEE")); // as named in Keycloak
	}

	@Test
	void employeeEndpointRejectsOtherRoles() throws Exception {
		mvc.perform(get("/api/v1/leaves/ping-employee")
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SUPERVISOR"))))
			.andExpect(status().isForbidden());
	}

	@Test
	void employeeEndpointAllowsEmployee() throws Exception {
		mvc.perform(get("/api/v1/leaves/ping-employee")
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Hello, employee"));
	}

}
