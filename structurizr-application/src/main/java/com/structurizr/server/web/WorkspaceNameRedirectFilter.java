package com.structurizr.server.web;

import com.structurizr.server.component.workspace.WorkspaceComponent;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetadata;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class WorkspaceNameRedirectFilter extends OncePerRequestFilter {

    private static final String WORKSPACE_PREFIX = "/workspace";

    private final WorkspaceComponent workspaceComponent;

    public WorkspaceNameRedirectFilter(WorkspaceComponent workspaceComponent) {
        this.workspaceComponent = workspaceComponent;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String routingKey = request.getParameter("key");
        if (routingKey == null || routingKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestUri = request.getRequestURI();
        if (requestUri == null || !requestUri.startsWith(WORKSPACE_PREFIX) || isCanonicalNumericWorkspaceUrl(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        WorkspaceMetadata workspaceMetadata = resolveWorkspaceByRoutingKey(routingKey);
        if (workspaceMetadata == null) {
            response.sendRedirect("/");
            return;
        }

        response.sendRedirect(buildRedirectUrl(requestUri, request, workspaceMetadata.getId(), routingKey));
    }

    private WorkspaceMetadata resolveWorkspaceByRoutingKey(String routingKey) {
        try {
            return workspaceComponent.getWorkspaceMetadataByRoutingKey(routingKey);
        } catch (WorkspaceComponentException e) {
            return null;
        }
    }

    private boolean isCanonicalNumericWorkspaceUrl(String requestUri) {
        String remainder = requestUri.substring(WORKSPACE_PREFIX.length());
        if (remainder.isEmpty() || "/".equals(remainder)) {
            return false;
        }

        if (!remainder.startsWith("/")) {
            return false;
        }

        int nextSlash = remainder.indexOf('/', 1);
        String firstSegment = nextSlash == -1 ? remainder.substring(1) : remainder.substring(1, nextSlash);
        return !firstSegment.isEmpty() && firstSegment.chars().allMatch(Character::isDigit);
    }

    private String buildRedirectUrl(String requestUri, HttpServletRequest request, long workspaceId, String routingKey) {
        String suffix = requestUri.substring(WORKSPACE_PREFIX.length());
        String normalizedRoutingKey = routingKey.trim();

        if (suffix.equals("/" + normalizedRoutingKey)) {
            suffix = "";
        } else if (suffix.startsWith("/" + normalizedRoutingKey + "/")) {
            suffix = suffix.substring(normalizedRoutingKey.length() + 1);
        }

        StringBuilder redirectUrl = new StringBuilder(WORKSPACE_PREFIX)
                .append('/')
                .append(workspaceId)
                .append(suffix);

        List<String> queryParameters = new ArrayList<>();
        appendQueryParameter(queryParameters, "branch", request.getParameter("branch"));
        appendQueryParameter(queryParameters, "version", request.getParameter("version"));

        if (!queryParameters.isEmpty()) {
            redirectUrl.append('?').append(String.join("&", queryParameters));
        }

        return redirectUrl.toString();
    }

    private void appendQueryParameter(List<String> queryParameters, String name, String value) {
        if (value != null && !value.isBlank()) {
            queryParameters.add(name + "=" + value);
        }
    }

}