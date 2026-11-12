package gm.gov.lms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Starts the whole app against a real Postgres (Testcontainers): Liquibase runs, Hibernate
 * validates, Flowable creates its tables in the workflow schema.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class LmsApplicationTests {

	@MockitoBean
	JwtDecoder jwtDecoder; // tests never call Keycloak

	@Test
	void contextLoads() {
	}

}
