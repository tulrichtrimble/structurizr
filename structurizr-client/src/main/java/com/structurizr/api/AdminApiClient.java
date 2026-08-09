package com.structurizr.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * A client for the Structurizr Admin API.
 */
public class AdminApiClient extends AbstractApiClient {

    private static final Log log = LogFactory.getLog(AdminApiClient.class);

    /**
     * Creates a new admin API client.
     *
     * @param url       the URL of your Structurizr instance
     * @param apiKey    the admin API key
     */
    public AdminApiClient(String url, String apiKey) {
        super(url, apiKey);
    }

    /**
     * Gets a list of all workspaces.
     *
     * @return  a List of WorkspaceMetadata objects
     * @throws StructurizrClientException   if an error occurs
     */
    public List<WorkspaceMetadata> getWorkspaces() throws StructurizrClientException {
        try (CloseableHttpClient httpClient = HttpClients.createSystem()) {
            log.debug("Getting workspaces");

            HttpUriRequestBase httpRequest;

            httpRequest = new HttpGet(url + WORKSPACE_PATH);

            addHeaders(httpRequest, "");
            debugRequest(httpRequest, null);

            HttpClientResult result = httpClient.execute(httpRequest, response -> {
                String json = EntityUtils.toString(response.getEntity());
                debugResponse(response, json);

                return new HttpClientResult(response.getCode() == HttpStatus.SC_OK, response.getCode(), json);
            });

            checkResponseIsJson(result.getContent());

            if (result.isSuccess()) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                Workspaces workspaces = objectMapper.readValue(result.getContent(), Workspaces.class);
                return workspaces.getWorkspaces();
            } else {
                ApiResponse apiResponse = ApiResponse.parse(result.getContent());
                throw new StructurizrClientException(apiResponse.getMessage());
            }

        } catch (Exception e) {
            log.error(e);
            throw new StructurizrClientException(e);
        }
    }

    /**
     * Creates a new workspace.
     *
     * @return  a WorkspaceMetadata object representing the new workspace
     * @throws StructurizrClientException   if an error occurs
     */
    public WorkspaceMetadata createWorkspace() throws StructurizrClientException {
        return createWorkspace(null);
    }

    /**
     * Creates a new workspace, or resolves an existing workspace by routing key.
     *
     * @param routingKey    the routing key to create or resolve
     * @return  a WorkspaceMetadata object representing the workspace
     * @throws StructurizrClientException   if an error occurs
     */
    public WorkspaceMetadata createWorkspace(String routingKey) throws StructurizrClientException {
        try (CloseableHttpClient httpClient = HttpClients.createSystem()) {
            log.debug("Creating workspace");

            String path = url + WORKSPACE_PATH;
            if (routingKey != null && !routingKey.isBlank()) {
                path += "?key=" + URLEncoder.encode(routingKey.trim(), StandardCharsets.UTF_8);
            }

            HttpUriRequestBase httpRequest = new HttpPost(path);

            addHeaders(httpRequest, "");
            debugRequest(httpRequest, null);

            HttpClientResult result = httpClient.execute(httpRequest, response -> {
                String json = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : "";
                debugResponse(response, json);

                return new HttpClientResult(response.getCode() == HttpStatus.SC_OK, response.getCode(), json);
            });

            if (result.isSuccess()) {
                checkResponseIsJson(result.getContent());
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper.readValue(result.getContent(), WorkspaceMetadata.class);
            } else {
                String content = result.getContent() == null ? "" : result.getContent().trim();
                if (content.startsWith("{")) {
                    ApiResponse apiResponse = ApiResponse.parse(content);
                    throw new StructurizrClientException(apiResponse.getMessage());
                }

                String message = "Admin API request failed with HTTP " + result.getStatusCode();
                if (!content.isEmpty()) {
                    message += ": " + content;
                }

                throw new StructurizrClientException(message);
            }
        } catch (StructurizrClientException e) {
            log.error(e);
            throw e;
        } catch (Exception e) {
            log.error(e);
            throw new StructurizrClientException(e);
        }
    }

    /**
     * Pushes a workspace JSON document by routing key.
     *
     * @param routingKey    the routing key
     * @param json          the workspace JSON
     * @throws StructurizrClientException   if an error occurs
     */
    public void putWorkspaceByKey(String routingKey, String json) throws StructurizrClientException {
        try (CloseableHttpClient httpClient = HttpClients.createSystem()) {
            log.debug("Pushing workspace by key " + routingKey);

            HttpPut httpRequest = new HttpPut(url + WORKSPACE_PATH + "/key/" + URLEncoder.encode(routingKey.trim(), StandardCharsets.UTF_8));
            httpRequest.setEntity(new StringEntity(json));

            addHeaders(httpRequest, "application/json; charset=UTF-8");
            debugRequest(httpRequest, json);

            HttpClientResult result = httpClient.execute(httpRequest, response -> {
                String responseJson = EntityUtils.toString(response.getEntity());
                debugResponse(response, responseJson);

                return new HttpClientResult(response.getCode() == HttpStatus.SC_OK, response.getCode(), responseJson);
            });

            checkResponseIsJson(result.getContent());
            ApiResponse apiResponse = ApiResponse.parse(result.getContent());

            if (!result.isSuccess()) {
                throw new StructurizrClientException(apiResponse.getMessage());
            }
        } catch (Exception e) {
            log.error(e);
            throw new StructurizrClientException(e);
        }
    }

    /**
     * Deletes a workspace.
     *
     * @param workspaceId       the ID of the workspace to delete
     * @return  true if successful, false otherwise
     * @throws StructurizrClientException   if an error occurs
     */
    public boolean deleteWorkspace(long workspaceId) throws StructurizrClientException {
        try (CloseableHttpClient httpClient = HttpClients.createSystem()) {
            log.debug("Deleting workspace " + workspaceId);

            HttpUriRequestBase httpRequest = new HttpDelete(url + WORKSPACE_PATH + "/" + workspaceId);

            addHeaders(httpRequest, "");
            debugRequest(httpRequest, null);

            HttpClientResult result = httpClient.execute(httpRequest, response -> {
                String json = EntityUtils.toString(response.getEntity());
                debugResponse(response, json);

                return new HttpClientResult(response.getCode() == HttpStatus.SC_OK, response.getCode(), json);
            });

            checkResponseIsJson(result.getContent());
            ApiResponse apiResponse = ApiResponse.parse(result.getContent());

            if (result.isSuccess()) {
                return apiResponse.isSuccess();
            } else {
                throw new StructurizrClientException(apiResponse.getMessage());
            }
        } catch (Exception e) {
            log.error(e);
            throw new StructurizrClientException(e);
        }
    }

    private static final class HttpClientResult {

        private final boolean success;
        private final int statusCode;
        private final String content;

        private HttpClientResult(boolean success, int statusCode, String content) {
            this.success = success;
            this.statusCode = statusCode;
            this.content = content;
        }

        private boolean isSuccess() {
            return success;
        }

        private int getStatusCode() {
            return statusCode;
        }

        private String getContent() {
            return content;
        }
    }

}