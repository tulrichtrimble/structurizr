package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.Permission;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.Views;
import com.structurizr.util.HtmlUtils;
import com.structurizr.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Controller
class DiagramViewerController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/diagrams", method = RequestMethod.GET, params = "name")
    String redirectToAuthenticatedDiagramViewerByName(
            @RequestParam("name") String workspaceName,
            @RequestParam(required = false, defaultValue = "") String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        WorkspaceMetadata workspaceMetadata = resolveWorkspaceByName(workspaceName);
        if (workspaceMetadata == null) {
            return show404Page(model);
        }

        return "redirect:" + buildDiagramViewerUrl(workspaceMetadata.getId(), branch, version);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/diagrams", method = RequestMethod.GET)
    String showAuthenticatedDiagramViewer(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false, defaultValue = "") String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        model.addAttribute("publishThumbnails", StringUtils.isNullOrEmpty(version));
        model.addAttribute("quickNavigationPath", "diagrams");

        if (Configuration.getInstance().getProfile() == com.structurizr.configuration.Profile.Local) {
            enableLocalRefresh(model);
        }

        return showAuthenticatedView(
                Views.DIAGRAMS, workspaceId,
                workspaceMetadata -> {
                    if (Configuration.getInstance().getProfile() == Profile.Local) {
                        model.addAttribute("includeEditButton", "true".equalsIgnoreCase(Configuration.getInstance().getProperty(StructurizrProperties.EDITABLE_PROPERTY)));
                    } else {
                        Set<Permission> permissions = workspaceMetadata.getPermissions(getUser());
                        model.addAttribute("includeEditButton", permissions.contains(Permission.Write));
                    }

                    if (Configuration.getInstance().getProfile() == Profile.Local) {
                        model.addAttribute("publishImages", false);
                    } else {
                        model.addAttribute("publishImages", !workspaceMetadata.isClientEncrypted());
                    }
                },
                branch, version, model, false, false
        );
    }

    private WorkspaceMetadata resolveWorkspaceByName(String workspaceName) {
        try {
            return workspaceComponent.getWorkspaceMetadata(workspaceName);
        } catch (WorkspaceComponentException e) {
            return null;
        }
    }

    private String buildDiagramViewerUrl(long workspaceId, String branch, String version) {
        StringBuilder url = new StringBuilder("/workspace/").append(workspaceId).append("/diagrams");
        boolean hasQuery = false;

        if (branch != null && !branch.isEmpty()) {
            url.append("?branch=").append(branch);
            hasQuery = true;
        }

        if (version != null && !version.isEmpty()) {
            url.append(hasQuery ? "&" : "?").append("version=").append(version);
        }

        return url.toString();
    }

}