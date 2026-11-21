package com.sqdstudios.easyrelease.cli.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.sqdstudios.easyrelease.cli.model.Channel;
import com.sqdstudios.easyrelease.cli.model.Config;
import com.sqdstudios.easyrelease.cli.model.Dependency;
import com.sqdstudios.easyrelease.cli.model.Loader;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.sqdstudios.easyrelease.cli.Main.*;

public class HangarPlatform implements Platform {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String API_BASE_URL = "https://hangar.papermc.io/api/v1";
    private static final HttpClient client = HttpClient.newHttpClient();

    public HangarPlatform() {
        log("Starting publishing to Hangar...");
    }

    @Override
    public CompletableFuture<Void> release(Config config, List<File> files, String version, String subtitle, String description, Channel channel, List<String> mcVersions, List<Loader> loaders) {
        return CompletableFuture.runAsync(() -> {
            try {
                String apiKey = config.getHangarApiKeyEnv();
                String author = config.getHangarAuthor();
                String slug = config.getHangarSlug();

                if (apiKey == null) {
                    error("Hangar API Key not configured. Skipping Hangar.");
                    return;
                }
                if (author == null || slug == null) {
                    error("Hangar Author or Slug not configured. Skipping Hangar.");
                    return;
                }

                if (files == null || files.isEmpty()) {
                    error("No files provided for Hangar release. Skipping.");
                    return;
                }

                if (files.size() > 1) {
                    log(YELLOW + "Hangar only supports one file per version. Only the first file will be uploaded." + RESET);
                }
                File file = files.getFirst();

                String token = getSessionToken(apiKey);
                if (token == null) {
                    error("Failed to get Hangar session token.");
                    return;
                }

                Map<String, Object> versionData = new HashMap<>();
                versionData.put("version", version);
                versionData.put("description", description);
                versionData.put("channel", channel.toString());

                Map<String, List<String>> platformDependencies = new HashMap<>();
                for (Loader loader : loaders) {
                    String hangarPlatform = getHangarPlatform(loader);
                    if (hangarPlatform != null) {
                        platformDependencies.put(hangarPlatform, mcVersions);
                    }
                }

                if (platformDependencies.isEmpty()) {
                    error("No compatible Hangar platforms found for loaders: " + loaders);
                    return;
                }

                versionData.put("platformDependencies", platformDependencies);

                // For dependencies on other Hangar projects
                Map<String, List<Map<String, Object>>> hangarDeps = new HashMap<>();
                if (config.getDependencies() != null) {
                    for (Dependency dep : config.getDependencies()) {
                        Map<String, Object> depObj = new HashMap<>();
                        depObj.put("name", dep.getName());
                        depObj.put("required", dep.isRequired());
                        if (dep.getExternalUrl() != null) {
                            depObj.put("externalUrl", dep.getExternalUrl());
                        }

                        if (dep.getHangarPlatform() != null) {
                            hangarDeps.computeIfAbsent(dep.getHangarPlatform(), k -> new ArrayList<>()).add(depObj);
                        } else {
                            // Apply to all platforms configured for this version
                            for (String platform : platformDependencies.keySet()) {
                                hangarDeps.computeIfAbsent(platform, k -> new ArrayList<>()).add(depObj);
                            }
                        }
                    }
                }
                versionData.put("pluginDependencies", hangarDeps);

                String boundary = "---" + System.currentTimeMillis();
                byte[] fileBytes = Files.readAllBytes(file.toPath());

                String jsonPart = mapper.writeValueAsString(versionData);

                String multipartPrefix = "--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"version-upload\"\r\n" +
                        "Content-Type: application/json\r\n\r\n" +
                        jsonPart + "\r\n" +
                        "--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"files\"; filename=\"" + file.getName() + "\"\r\n" +
                        "Content-Type: application/octet-stream\r\n\r\n";

                byte[] start = multipartPrefix.getBytes();
                byte[] end = ("\r\n--" + boundary + "--\r\n").getBytes();

                byte[] body = new byte[start.length + fileBytes.length + end.length];
                System.arraycopy(start, 0, body, 0, start.length);
                System.arraycopy(fileBytes, 0, body, start.length, fileBytes.length);
                System.arraycopy(end, 0, body, start.length + fileBytes.length, end.length);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE_URL + "/projects/" + author + "/" + slug + "/versions/upload"))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .header("Authorization", "Bearer " + token)
                        .header("User-Agent", "sqdstudios/easyrelease-cli")
                        .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    log("Successfully uploaded to Hangar!");
                } else {
                    error("Failed to upload to Hangar. Status: " + response.statusCode());
                    error("Response: " + response.body());
                }

            } catch (Exception e) {
                error(RED + "Error releasing to Hangar: " + e.getMessage() + RESET);
                e.printStackTrace();
            }
        });
    }

    private String getSessionToken(String apiKey) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/authenticate?apiKey=" + apiKey))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            Map<String, Object> res = mapper.readValue(response.body(), TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));
            return (String) res.get("token");
        }
        return null;
    }

    private String getHangarPlatform(Loader loader) {
        return switch (loader) {
            case paper, spigot, bukkit, purpur, folia -> "PAPER";
            case velocity -> "VELOCITY";
            case bungeecord, waterfall -> "WATERFALL";
            default -> null;
        };
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
