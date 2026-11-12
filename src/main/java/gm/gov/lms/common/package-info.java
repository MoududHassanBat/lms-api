/**
 * Shared platform module: audit, error model, domain events, file storage, notifications.
 * Every business module may use it; it depends on no business module.
 */
@ApplicationModule(displayName = "Common")
package gm.gov.lms.common;

import org.springframework.modulith.ApplicationModule;
