package gm.gov.lms;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	/** Same image and same schema script as compose.yaml, so tests see the real layout. */
	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(DockerImageName.parse("postgres:18"))
			.withCopyFileToContainer(MountableFile.forHostPath("db/init/01-schemas.sql"),
					"/docker-entrypoint-initdb.d/01-schemas.sql");
	}

}
