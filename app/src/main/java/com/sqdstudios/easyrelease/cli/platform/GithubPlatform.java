package com.sqdstudios.easyrelease.cli.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqdstudios.easyrelease.cli.model.Channel;
import com.sqdstudios.easyrelease.cli.model.Config;
import com.sqdstudios.easyrelease.cli.model.Loader;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.sqdstudios.easyrelease.cli.Main.RED;
import static com.sqdstudios.easyrelease.cli.Main.RESET;

public class GithubPlatform implements Platform {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public CompletableFuture<Void> release(Config config, File file, String version, String subtitle, String description, Channel channel, List<String> mcVersions, List<Loader> loaders) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (config.getGithubRepo() == null || config.getGithubToken(false) == null) {
                    error("GitHub Repository or Token not configured. Skipping GitHub.");
                    return;
                }

                String repo = config.getGithubRepo();
                String createReleaseUrl = "https://api.github.com/repos/" + repo + "/releases";

                Map<String, Object> releaseData = new HashMap<>();
                releaseData.put("tag_name", version);
                releaseData.put("target_commitish", config.getMainBranch() != null ? config.getMainBranch() : "main");
                releaseData.put("name", subtitle);
                releaseData.put("body", description);
                releaseData.put("draft", false);
                releaseData.put("prerelease", channel != Channel.release);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest createRequest = HttpRequest.newBuilder()
                        .uri(URI.create(createReleaseUrl))
                        .header("Authorization", "Bearer " + config.getGithubToken(false))
                        .header("Accept", "application/vnd.github+json")
                        .header("User-Agent", "sqdstudios/easyrelease-cli")
                        .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(releaseData)))
                        .build();

                HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
                if (createResponse.statusCode() < 200 || createResponse.statusCode() >= 300) {
                    error("Failed to create GitHub release. Status: " + createResponse.statusCode());
                    error("Response: " + createResponse.body());
                    return;
                }

                JsonNode releaseNode = mapper.readTree(createResponse.body());
                String uploadUrl = releaseNode.get("upload_url").asText().split("\\{")[0];
                uploadUrl += "?name=" + file.getName();

                byte[] fileBytes = Files.readAllBytes(file.toPath());
                HttpRequest uploadRequest = HttpRequest.newBuilder()
                        .uri(URI.create(uploadUrl))
                        .header("Authorization", "Bearer " + config.getGithubToken(false))
                        .header("Content-Type", "application/octet-stream")
                        .header("User-Agent", "sqdstudios/easyrelease-cli")
                        .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                        .build();

                HttpResponse<String> uploadResponse = client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());
                if (uploadResponse.statusCode() >= 200 && uploadResponse.statusCode() < 300) {
                    log("Successfully uploaded to GitHub!");
                } else {
                    error("Failed to upload asset to GitHub. Status: " + uploadResponse.statusCode());
                    error("Response: " + uploadResponse.body());
                }
            } catch (Exception e) {
                error(RED + "Error releasing to github:" + e.getMessage() + RESET);
                error(Arrays.toString(e.getStackTrace()));
            }
        });
    }

    private void log(String message) {
        synchronized (System.out) {
            System.out.println(message);
        }
    }

    private void error(String message) {
        synchronized (System.err) {
            System.err.println(message);
        }
    }
}
