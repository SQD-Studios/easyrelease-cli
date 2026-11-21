package com.sqdstudios.easyrelease.cli.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    public CompletableFuture<Void> release(Config config, List<File> files, String version, String subtitle, String description, Channel channel, List<String> mcVersions, List<Loader> loaders) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (config.getModrinthProjectId() == null) {
                    error("Modrinth Project ID not configured. Skipping Modrinth.");
                    return;
                } else if (config.getModrinthTokenEnv() == null) {
                    error("Modrinth Token not configured. Skipping Modrinth.");
                }

                if (files == null || files.isEmpty()) {
                    error("No files provided for Modrinth release. Skipping.");
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

                List<String> fileParts = new ArrayList<>();
                for (int i = 0; i < files.size(); i++) {
                    fileParts.add("file" + i);
                }
                data.put("file_parts", fileParts);
                data.put("primary_file", "file0");

                String boundary = "---" + System.currentTimeMillis();
                
                StringBuilder multipartHeader = new StringBuilder();
                multipartHeader.append("--").append(boundary).append("\r\n")
                        .append("Content-Disposition: form-data; name=\"data\"\r\n\r\n")
                        .append(mapper.writeValueAsString(data)).append("\r\n");

                List<byte[]> parts = new ArrayList<>();
                parts.add(multipartHeader.toString().getBytes());

                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    String filePartHeader = "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"file" + i + "\"; filename=\"" + file.getName() + "\"\r\n" +
                            "Content-Type: application/octet-stream\r\n\r\n";
                    parts.add(filePartHeader.getBytes());
                    parts.add(Files.readAllBytes(file.toPath()));
                    parts.add("\r\n".getBytes());
                }
                parts.add(("--" + boundary + "--\r\n").getBytes());

                int totalLength = 0;
                for (byte[] part : parts) totalLength += part.length;
                byte[] body = new byte[totalLength];
                int currentPos = 0;
                for (byte[] part : parts) {
                    System.arraycopy(part, 0, body, currentPos, part.length);
                    currentPos += part.length;
                }

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .header("Authorization", config.getModrinthTokenEnv())
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
            for (Dependency entry : config.getDependencies()) {
                Map<String, Object> dep = new HashMap<>();
                dep.put("project_id", entry.getName());
                dep.put("dependency_type", entry.getType());
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
