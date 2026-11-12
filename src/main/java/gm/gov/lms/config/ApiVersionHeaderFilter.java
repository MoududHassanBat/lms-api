package gm.gov.lms.config;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Adds {@code X-API-Version} (from {@code api.version}) to every response. Runs before
 * Spring Security and sets the header before the chain, so 401/403/error responses carry it too.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class ApiVersionHeaderFilter extends OncePerRequestFilter {

	static final String HEADER = "X-API-Version";

	private final String apiVersion;

	ApiVersionHeaderFilter(@Value("${api.version}") String apiVersion) {
		this.apiVersion = apiVersion;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		response.setHeader(HEADER, apiVersion);
		chain.doFilter(request, response);
	}

}
