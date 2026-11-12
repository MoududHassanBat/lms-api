package gm.gov.lms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API docs for Swagger UI. Training only: the OAuth2 password flow lets Swagger UI's
 * <em>Authorize</em> button log in with a Keycloak username and password (the client needs
 * Direct Access Grants on). Swagger UI is switched off in prod.
 */
@Configuration
class OpenApiConfig {

	private static final String SCHEME = "keycloak";

	@Bean
	OpenAPI openApi(@Value("${api.version}") String apiVersion,
			@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}") String issuerUri) {
		var passwordFlow = new OAuthFlow().tokenUrl(issuerUri + "/protocol/openid-connect/token");
		return new OpenAPI()
			.info(new Info().title("LMS API").version(apiVersion)
				.description("Training Artifact — Not for Production Use"))
			.components(new Components().addSecuritySchemes(SCHEME, new SecurityScheme()
				.type(SecurityScheme.Type.OAUTH2)
				.flows(new OAuthFlows().password(passwordFlow))))
			.addSecurityItem(new SecurityRequirement().addList(SCHEME)); // every endpoint sends the token
	}

}
