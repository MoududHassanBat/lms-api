package gm.gov.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * One schema per module, nothing in {@code public}: schemas come from the DBA init
 * script ({@code db/init}), Liquibase only creates tables inside them, and Flowable keeps
 * its tables in {@code workflow} — without any identity tables (Keycloak owns identity).
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class SchemaLayoutTests {

	// issuer-uri is only set in the local profile; tests never call Keycloak.
	@MockitoBean
	JwtDecoder jwtDecoder;

	@Autowired
	JdbcTemplate jdbc;

	@Test
	void moduleSchemasExist() {
		assertThat(jdbc.queryForList(
				"SELECT schema_name FROM information_schema.schemata", String.class))
			.contains("request", "review", "tracking", "workflow", "common");
	}

	@Test
	void noTableInPublicSchema() {
		assertThat(tablesIn("public")).isEmpty();
	}

	@Test
	void liquibaseAndPlatformTablesLiveInCommon() {
		assertThat(tablesIn("common")).contains("databasechangelog", "databasechangeloglock",
				"event_publication", "system_info");
	}

	@Test
	void flowableEngineTablesLiveInWorkflow() {
		assertThat(tablesIn("workflow")).contains("act_re_procdef", "act_ru_execution", "act_ru_task",
				"act_hi_procinst");
	}

	@Test
	void flowableKeepsNoIdentityTables() {
		assertThat(tablesIn("workflow")).noneMatch(table -> table.startsWith("act_id_"));
	}

	@Test
	void flowableEventRegistryIsOff() {
		assertThat(tablesIn("workflow"))
			.noneMatch(table -> table.startsWith("flw_ev") || table.startsWith("flw_channel")
					|| table.startsWith("flw_event"));
	}

	private List<String> tablesIn(String schema) {
		return jdbc.queryForList(
				"SELECT table_name FROM information_schema.tables WHERE table_schema = ?",
				String.class, schema);
	}

}
