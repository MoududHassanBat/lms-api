package gm.gov.lms;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Fails the build when a module reaches into another module's internals or uses a
 * dependency not listed in its {@code allowedDependencies}. Needs no Spring context or DB.
 */
class ModularityTests {

	static final ApplicationModules modules = ApplicationModules.of(LmsApplication.class);

	@Test
	void verifiesModuleBoundaries() {
		modules.verify();
	}

	@Test
	void printsModules() {
		modules.forEach(System.out::println);
	}

}
