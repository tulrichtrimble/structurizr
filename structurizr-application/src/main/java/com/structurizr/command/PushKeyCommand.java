package com.structurizr.command;

import com.structurizr.Workspace;
import com.structurizr.api.AdminApiClient;
import com.structurizr.api.WorkspaceApiClient;
import com.structurizr.api.WorkspaceMetadata;
import com.structurizr.encryption.AesEncryptionStrategy;
import com.structurizr.io.json.JsonWriter;
import com.structurizr.util.StringUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.StringWriter;

public class PushKeyCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(PushKeyCommand.class);

    public PushKeyCommand() {
        super("push-key");
    }

    @Override
    public void run(String... args) throws Exception {
        Options options = new Options();

        Option option = new Option("url", "apiUrl", true, "Structurizr API URL");
        option.setRequired(true);
        options.addOption(option);

        option = new Option(null, "adminApiKey", true, "Admin API key");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("w", "workspace", true, "Path or URL to the workspace JSON/DSL file");
        option.setRequired(true);
        options.addOption(option);

        option = new Option("branch", "branch", true, "Branch name");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("passphrase", "passphrase", true, "Client-side encryption passphrase");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("merge", "mergeFromRemote", true, "Whether to merge layout information from the remote workspace (default=true)");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("trim", "trim", true, "Whether to trim the workspace before pushing (default=false)");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("archive", "archive", true, "Stores the previous version of the remote workspace");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("debug", "debug", false, "Enable debug logging");
        option.setRequired(false);
        options.addOption(option);

        CommandLineParser commandLineParser = new DefaultParser();

        String apiUrl;
        String apiKey;
        String workspaceKey;
        String workspacePath;
        String branch;
        String passphrase;
        boolean mergeFromRemote;
        boolean trim;
        boolean archive;
        boolean debug;

        try {
            CommandLine cmd = commandLineParser.parse(options, args);

            apiUrl = cmd.getOptionValue("apiUrl");
            apiKey = cmd.getOptionValue("adminApiKey");
            workspacePath = cmd.getOptionValue("workspace");
            branch = cmd.getOptionValue("branch");
            passphrase = cmd.getOptionValue("passphrase");
            mergeFromRemote = Boolean.parseBoolean(cmd.getOptionValue("merge", "true"));
            trim = Boolean.parseBoolean(cmd.getOptionValue("trim", "false"));
            archive = Boolean.parseBoolean(cmd.getOptionValue("archive", "true"));
            debug = cmd.hasOption("debug");
        } catch (ParseException e) {
            log.error(e.getMessage());
            showHelp(options);
            System.exit(1);
            return;
        }

        if (debug) {
            configureDebugLogging();
        }

        Workspace workspace = loadWorkspace(workspacePath);

        workspaceKey = workspace.getProperties().get("key");

        if (StringUtils.isNullOrEmpty(workspaceKey)) {
            log.error("define a workspace key in you workspace.dsl as properties {\nkey \"uniqueSystemName\"\n}");
            System.exit(1);
            return;
        }

        if (trim) {
            log.info(" - trimming workspace");
            workspace.trim();
        }

        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(true);
        jsonWriter.write(workspace, stringWriter);

        AdminApiClient client = new AdminApiClient(apiUrl, apiKey);
        client.setAgent(getAgent());
        WorkspaceMetadata workspaceMetadata = client.createWorkspace(workspaceKey);

        WorkspaceApiClient workspaceApiClient = new WorkspaceApiClient(apiUrl, workspaceMetadata.getId(), workspaceMetadata.getApiKey());
        workspaceApiClient.setBranch(branch);
        workspaceApiClient.setAgent(getAgent());
        workspaceApiClient.setMergeFromRemote(mergeFromRemote);
        workspaceApiClient.setWorkspaceArchiveLocation(null);

        if (!StringUtils.isNullOrEmpty(passphrase)) {
            log.info(" - using client-side encryption");
            workspaceApiClient.setEncryptionStrategy(new AesEncryptionStrategy(passphrase));
        }

        File archivePath = new File(workspacePath).getParentFile();
        if (archive) {
            workspaceApiClient.setWorkspaceArchiveLocation(archivePath);
            log.info(" - storing previous version of workspace in " + workspaceApiClient.getWorkspaceArchiveLocation());
        }

        if (StringUtils.isNullOrEmpty(branch)) {
            log.info("Pushing workspace " + workspaceKey + " to " + apiUrl);
        } else {
            log.info("Pushing workspace " + workspaceKey + " to " + apiUrl + " (branch=" + branch + ")");
        }
        log.info(" - merge layout from remote: " + mergeFromRemote);
        log.info(" - pushing workspace");
        workspaceApiClient.putWorkspace(workspace);
        log.info(" - finished");
    }
}