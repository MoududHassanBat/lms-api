/**
 * Module 2 — Review &amp; Approval with the return-for-revision loop (mirror of iDCMS memoReview).
 * Reads leave requests through the request module's {@code api} package only.
 */
@ApplicationModule(displayName = "Leave Review", allowedDependencies = { "common", "workflow :: api", "request :: api" })
package gm.gov.lms.review;

import org.springframework.modulith.ApplicationModule;
