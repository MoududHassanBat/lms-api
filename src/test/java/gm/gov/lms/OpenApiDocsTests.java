package gm.gov.lms;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Swagger UI can log in with a Keycloak username and password (training only): the API docs
 * declare an OAuth2 password flow against the realm's token endpoint, required on every call.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://idp.example/realms/TEST")
class OpenApiDocsTests {

	@Autowired
	MockMvc mvc;

	@MockitoBean
	JwtDecoder jwtDecoder; // tests never call Keycloak

	@Test
	void apiDocsDeclareKeycloakPasswordFlow() throws Exception {
		mvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.components.securitySchemes.keycloak.type").value("oauth2"))
			.andExpect(jsonPath("$.components.securitySchemes.keycloak.flows.password.tokenUrl")
				.value("https://idp.example/realms/TEST/protocol/openid-connect/token"))
			.andExpect(jsonPath("$.security[0].keycloak").isArray());
	}

}
