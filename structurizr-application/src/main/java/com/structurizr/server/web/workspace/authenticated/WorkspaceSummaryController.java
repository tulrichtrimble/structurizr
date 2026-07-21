package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.configuration.Profile;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.Views;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class WorkspaceSummaryController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace", method = RequestMethod.GET, params = "name")
    public String redirectToWorkspaceSummaryByName(
            @RequestParam("name") String workspaceName,
            @RequestParam(required = false, defaultValue = "") String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        WorkspaceMetadata workspaceMetadata = resolveWorkspaceByName(workspaceName);
        if (workspaceMetadata == null) {
            return show404Page(model);
        }

        return "redirect:" + buildWorkspaceSummaryUrl(workspaceMetadata.getId(), branch, version);
    }

    @RequestMapping(value = "/workspace/{workspaceId}", method = RequestMethod.GET)
    public String showAuthenticatedWorkspaceSummary(
            @PathVariable("workspaceId") String workspaceId,
            @RequestParam(required = false, defaultValue = "") String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        Long numericWorkspaceId = parseWorkspaceId(workspaceId);
        if (numericWorkspaceId == null) {
            WorkspaceMetadata workspaceMetadata = resolveWorkspaceByName(workspaceId);
            if (workspaceMetadata == null) {
                return show404Page(model);
            }

            return "redirect:" + buildWorkspaceSummaryUrl(workspaceMetadata.getId(), branch, version);
        }

        if (Configuration.getInstance().getProfile() == com.structurizr.configuration.Profile.Local) {
            enableLocalRefresh(model);
        }

        return showAuthenticatedView(
                Views.WORKSPACE_SUMMARY, numericWorkspaceId,
                workspaceMetadata -> {
                    if (Configuration.getInstance().getProfile() == Profile.Server) {
                        if (Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
                            model.addAttribute("branchesEnabled", true);
                            model.addAttribute("branch", branch);
                            model.addAttribute("branches", workspaceComponent.getWorkspaceBranches(numericWorkspaceId));
                        }

                        model.addAttribute("versions", workspaceComponent.getWorkspaceVersions(numericWorkspaceId, branch));
                    }
                },
                branch, version, model, true, true
        );
    }

    private WorkspaceMetadata resolveWorkspaceByName(String workspaceName) {
        try {
            return workspaceComponent.getWorkspaceMetadata(workspaceName);
        } catch (WorkspaceComponentException e) {
            return null;
        }
    }

    private Long parseWorkspaceId(String workspaceId) {
        try {
            return Long.parseLong(workspaceId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String buildWorkspaceSummaryUrl(long workspaceId, String branch, String version) {
        StringBuilder url = new StringBuilder("/workspace/").append(workspaceId);
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