package com.structurizr.server.web.api;

import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.MockHttpServletResponse;
import com.structurizr.server.web.MockWorkspaceComponent;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AdminApiControllerTests {

    @Test
    void createWorkspace_ReturnsExistingWorkspace_WhenRoutingKeyAlreadyExists() {
        WorkspaceMetadata existing = new WorkspaceMetadata(7);
        existing.setRoutingKey("dewey");

        AdminApiController controller = new AdminApiController();
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadataByRoutingKey(String routingKey) {
                return existing;
            }
        });

        WorkspaceMetadata result = controller.createWorkspace(null, System.getenv("ADMIN_API_KEY"), "dewey");

        assertSame(existing, result);
    }

    @Test
    void createWorkspace_PersistsRoutingKey_OnNewWorkspace() {
        WorkspaceMetadata created = new WorkspaceMetadata(1);
        WorkspaceMetadata[] persisted = new WorkspaceMetadata[1];

        AdminApiController controller = new AdminApiController();
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadataByRoutingKey(String routingKey) {
                return null;
            }

            @Override
            public long createWorkspace(com.structurizr.server.domain.User user) {
                return 1;
            }

            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return created;
            }

            @Override
            public void putWorkspaceMetadata(WorkspaceMetadata workspaceMetaData) {
                persisted[0] = workspaceMetaData;
            }
        });

        WorkspaceMetadata result = controller.createWorkspace(null, System.getenv("ADMIN_API_KEY"), "dewey");

        assertSame(created, result);
        assertEquals("dewey", created.getRoutingKey());
        assertSame(created, persisted[0]);
    }

    @Test
    void createWorkspace_ThrowsApiException_WhenMetadataCannotBeLoadedAfterCreation() {
        AdminApiController controller = new AdminApiController();
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadataByRoutingKey(String routingKey) {
                return null;
            }

            @Override
            public long createWorkspace(com.structurizr.server.domain.User user) {
                return 1;
            }

            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        ApiException exception = assertThrows(ApiException.class,
                () -> controller.createWorkspace(null, System.getenv("ADMIN_API_KEY"), "dewey"));

        assertEquals("Workspace metadata could not be loaded after creation", exception.getMessage());
    }

    @Test
    void error_ReturnsInternalServerErrorResponse_ForUncheckedExceptions() {
        AdminApiController controller = new AdminApiController();
        MockHttpServletResponse response = new MockHttpServletResponse();

        ApiResponse apiResponse = controller.error(new IllegalStateException("Lookup failed"), response);

        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getStatus());
        assertFalse(apiResponse.isSuccess());
        assertEquals("Lookup failed", apiResponse.getMessage());
    }
}