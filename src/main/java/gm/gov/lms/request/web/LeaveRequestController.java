package gm.gov.lms.request.web;

import java.util.Map;

import gm.gov.lms.request.web.dto.WhoAmIResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dummy API for the Day 1 demo: call it from Postman after an OIDC login. The real
 * leave endpoints are the assignment for branch {@code 02-leave-request}.
 */
@RestController
@RequestMapping("/api/v1/leaves")
class LeaveRequestController {

	private static final String ROLE_PREFIX = "ROLE_";

	@GetMapping("/whoami")
	@PreAuthorize("isAuthenticated()")
	WhoAmIResponse whoAmI(@AuthenticationPrincipal Jwt jwt, JwtAuthenticationToken authentication) {
		// Only the realm roles, named as in Keycloak (ROLE_EMPLOYEE -> EMPLOYEE); skips
		// non-role authorities such as Spring Security 7's FACTOR_BEARER.
		var roles = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.filter(authority -> authority.startsWith(ROLE_PREFIX))
			.map(authority -> authority.substring(ROLE_PREFIX.length()))
			.sorted()
			.toList();
		return new WhoAmIResponse(jwt.getClaimAsString("preferred_username"), roles);
	}

	@GetMapping("/ping-employee")
	@PreAuthorize("hasRole('EMPLOYEE')")
	Map<String, String> pingEmployee() {
		return Map.of("message", "Hello, employee");
	}

}
