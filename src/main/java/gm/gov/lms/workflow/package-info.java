/**
 * Central workflow engine: Flowable configuration, BPMN in {@code resources/processes},
 * task inbox. Never imports a business module; modules use its {@code api} package.
 */
@ApplicationModule(displayName = "Workflow", allowedDependencies = "common")
package gm.gov.lms.workflow;

import org.springframework.modulith.ApplicationModule;
