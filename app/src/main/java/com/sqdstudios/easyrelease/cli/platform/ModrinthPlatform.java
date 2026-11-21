package com.sqdstudios.easyrelease.cli.platform;

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
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.sqdstudios.easyrelease.cli.Main.RED;
import static com.sqdstudios.easyrelease.cli.Main.RESET;

public class ModrinthPlatform implements Platform {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String API_URL = "https://api.modrinth.com/v2/version";

    public ModrinthPlatform() {
        log("Starting publishing to Modrinth...");
    }

    @Override
    public CompletableFuture<Void> release(Config config, File file, String version, String subtitle, String description, Channel channel, List<String> mcVersions, List<Loader> loaders) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (config.getModrinthProjectId() == null || config.getModrinthToken(false) == null) {
                    error("Modrinth Project ID or Token not configured. Skipping Modrinth.");
                    return;
                }

                Map<String, Object> data = new HashMap<>();
                data.put("name", subtitle);
                data.put("version_number", version);
                data.put("changelog", description);

                List<Map<String, Object>> dependencies = getDependencies(config);
                data.put("dependencies", dependencies);

                data.put("game_versions", mcVersions);
                data.put("version_type", channel.toString());
                data.put("loaders", loaders.stream().map(Loader::getModrinthType).toList());
                data.put("featured", true);
                data.put("project_id", config.getModrinthProjectId());
                data.put("file_parts", List.of("file"));

                String boundary = "---" + System.currentTimeMillis();
                byte[] fileBytes = Files.readAllBytes(file.toPath());

                String multipartBody = "--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"data\"\r\n\r\n" +
                        mapper.writeValueAsString(data) + "\r\n" +
                        "--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n" +
                        "Content-Type: application/octet-stream\r\n\r\n";

                byte[] start = multipartBody.getBytes();
                byte[] end = ("\r\n--" + boundary + "--\r\n").getBytes();

                byte[] body = new byte[start.length + fileBytes.length + end.length];
                System.arraycopy(start, 0, body, 0, start.length);
                System.arraycopy(fileBytes, 0, body, start.length, fileBytes.length);
                System.arraycopy(end, 0, body, start.length + fileBytes.length, end.length);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .header("Authorization", config.getModrinthToken(false))
                        .header("User-Agent", "sqdstudios/easyrelease-cli")
                        .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    log("Successfully uploaded to Modrinth!");
                } else {
                    error("Failed to upload to Modrinth. Status: " + response.statusCode());
                    error("Response: " + response.body());
                }
            } catch (Exception e) {
                error(RED + "Error releasing to modrinth:" + e.getMessage() + RESET);
                error(Arrays.toString(e.getStackTrace()));
            }
        });
    }

    private static List<Map<String, Object>> getDependencies(Config config) {
        List<Map<String, Object>> dependencies = new ArrayList<>();
        if (config.getDependencies() != null) {
            for (Map.Entry<String, String> entry : config.getDependencies().entrySet()) {
                Map<String, Object> dep = new HashMap<>();
                dep.put("project_id", entry.getKey());
                dep.put("dependency_type", entry.getValue());
                dependencies.add(dep);
            }
        }
        return dependencies;
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
