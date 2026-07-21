package com.structurizr.server.web.api;

import com.structurizr.api.HttpHeaders;
import com.structurizr.server.web.AbstractController;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

/**
 * An implementation of the Structurizr admin API that throws exceptions to indicate this feature is not available.
 */
@RestController
@Profile("command-server & open-core")
public class NoOpAdminApiController extends AbstractController {

    private static final String MESSAGE = "The admin API is not supported in the open core version of the Structurizr server";

    private String resolveApiCredential(String xAuthorization, String authorization) {
        if (xAuthorization != null && !xAuthorization.trim().isEmpty()) {
            return xAuthorization;
        }

        return authorization;
    }

    @RequestMapping(value = "/api/workspace", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public void getWorkspaces(
            @RequestHeader(name = HttpHeaders.X_AUTHORIZATION, required = false) String xAuthorization,
            @RequestHeader(name = org.springframework.http.HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {

        String apiKey = resolveApiCredential(xAuthorization, authorization);

        throw new ApiException(MESSAGE);
    }

    @RequestMapping(value = "/api/workspace", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public void createWorkspace(
            @RequestHeader(name = HttpHeaders.X_AUTHORIZATION, required = false) String xAuthorization,
            @RequestHeader(name = org.springframework.http.HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {

        String apiKey = resolveApiCredential(xAuthorization, authorization);

        throw new ApiException(MESSAGE);
    }

    @RequestMapping(value = "/api/workspace/{workspaceId}", method = RequestMethod.DELETE, produces = "application/json; charset=UTF-8")
    public void deleteWorkspace(
            @RequestHeader(name = HttpHeaders.X_AUTHORIZATION, required = false) String xAuthorization,
            @RequestHeader(name = org.springframework.http.HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable("workspaceId") long workspaceId
    ) {

        String apiKey = resolveApiCredential(xAuthorization, authorization);

        throw new ApiException(MESSAGE);
    }

    @ExceptionHandler(ApiException.class)
    @ResponseBody
    public ApiResponse handleCustomException(ApiException exception, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return new ApiResponse(exception);
    }
    
}