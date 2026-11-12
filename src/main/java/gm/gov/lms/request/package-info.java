/**
 * Module 1 — Leave Application Submission (mirror of iDCMS memo submission).
 * Other modules may use only the {@code api} package.
 */
@ApplicationModule(displayName = "Leave Request", allowedDependencies = { "common", "workflow :: api" })
package gm.gov.lms.request;

import org.springframework.modulith.ApplicationModule;
