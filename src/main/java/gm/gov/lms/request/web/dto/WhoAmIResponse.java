package gm.gov.lms.request.web.dto;

import java.util.List;

/** Who the API thinks the caller is: the Keycloak username and realm roles. */
public record WhoAmIResponse(String username, List<String> roles) {
}
