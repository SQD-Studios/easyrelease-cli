package com.sqdstudios.easyrelease.cli;

import com.sqdstudios.easyrelease.cli.model.*;
import com.sqdstudios.easyrelease.cli.platform.GithubPlatform;
import com.sqdstudios.easyrelease.cli.platform.HangarPlatform;
import com.sqdstudios.easyrelease.cli.platform.ModrinthPlatform;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Main {

    public static final String RESET = "\u001B[0m \n";
    public static final String NO_NL = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String CYAN = "\u001B[36m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String RED = "\u001B[31m";
    public static final String PURPLE = "\u001B[35m";
    private static final Scanner sc = new Scanner(System.in);

    static synchronized void main(String[] args) {
        try {
            Config config = ConfigManager.loadConfig();

            if (args.length > 0 && args[0].equals("config")) {
                config(config);
                System.exit(0);
            }
            release(config);
        } catch (NoSuchElementException elementException) {
            System.out.println(RESET + PURPLE + "You stopped the input, assuming you were to close the program" + NO_NL);
            System.exit(0);
        }
    }

    public static void release(Config config) {
        System.out.println(PURPLE + BOLD + "--- EasyRelease CLI ---" + RESET);

        System.out.print(CYAN + "Tag/Version Number: " + RESET);
        String version = sc.nextLine();

        System.out.print(CYAN + "Version Subtitle: " + RESET);
        String subtitle = sc.nextLine();

        System.out.println(CYAN + "Changelog - (Write :wq to stop):" + NO_NL);
        StringBuilder descriptionBuilder = new StringBuilder();
        while (true) {
            String line = sc.nextLine();
            if (line.equals(":wq")) break;
            if (!descriptionBuilder.isEmpty()) descriptionBuilder.append("\n");
            descriptionBuilder.append(line);
        }
        String description = descriptionBuilder.toString();

        List<Map.Entry<File, List<Platform>>> releaseFiles = new ArrayList<>();
        System.out.println(CYAN + "Files to release - (Write :wq to stop):" + NO_NL);
        while (true) {
            System.out.print(CYAN + "File Path (e.g., build/libs/plugin.jar): " + RESET);
            String filePath = sc.nextLine();
            if (filePath.equals(":wq")) break;

            if (filePath.length() >= 2 && filePath.startsWith("\"") && filePath.endsWith("\"")) {
                filePath = filePath.substring(1, filePath.length() - 1);
            }
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println(RED + "File not found: " + file.getAbsolutePath() + RESET);
                continue;
            }

            System.out.print(CYAN + "Platforms for this file (github, modrinth, hangar) [all]: " + RESET);
            String platformsInput = sc.nextLine();
            List<Platform> platforms = new ArrayList<>();
            if (platformsInput.isBlank() || platformsInput.equalsIgnoreCase("all")) {
                if (config.getPlatforms() != null && !config.getPlatforms().isEmpty()) {
                    platforms.addAll(config.getPlatforms());
                } else {
                    platforms.addAll(Arrays.asList(Platform.values()));
                }
            } else {
                for (String p : platformsInput.split(",")) {
                    try {
                        platforms.add(Platform.valueOf(p.trim().toLowerCase()));
                    } catch (Exception ignored) {
                    }
                }
            }
            releaseFiles.add(new AbstractMap.SimpleEntry<>(file, platforms));
        }

        if (releaseFiles.isEmpty()) {
            System.err.println(RED + "No files selected for release." + RESET);
            System.exit(-3);
        }

        Set<Platform> allSelectedPlatforms = new HashSet<>();
        for (var entry : releaseFiles) {
            allSelectedPlatforms.addAll(entry.getValue());
        }

        if (allSelectedPlatforms.isEmpty()) {
            System.out.println(YELLOW + "No valid platforms selected." + RESET);
            System.exit(-2);
        }

        Channel channel;
        if (allSelectedPlatforms.contains(Platform.modrinth) || allSelectedPlatforms.contains(Platform.hangar)) {
            if (allSelectedPlatforms.contains(Platform.modrinth))
                System.out.print(CYAN + "Channel (release, beta, alpha): " + RESET);
            else
                System.out.print(CYAN + "Channel: " + RESET);
            channel = getChannel(allSelectedPlatforms);
        } else {
            System.out.print(CYAN + "Prerelease (true/false): " + RESET);
            String prerelease = sc.nextLine();
            if (Boolean.parseBoolean(prerelease)) channel = Channel.BasicChannel.beta;
            else channel = Channel.BasicChannel.release;
        }

        List<String> mcVersions = new ArrayList<>();
        List<Loader> loaders = new ArrayList<>();
        if (allSelectedPlatforms.contains(Platform.modrinth) || allSelectedPlatforms.contains(Platform.hangar)) {
            mcVersions = config.getMinecraftVersions();
            if (mcVersions == null || mcVersions.isEmpty()) {
                System.out.print(CYAN + "Minecraft Versions (comma separated): " + RESET);
                mcVersions = Arrays.asList(sc.nextLine().split(","));
            }

            loaders = config.getLoaders();
            if (loaders == null || loaders.isEmpty()) {
                System.out.print(CYAN + "Loaders (e.g., paper, fabric - comma separated): " + RESET);
                String[] loaderStrings = sc.nextLine().split(",");
                loaders = new ArrayList<>();
                for (String s : loaderStrings) {
                    try {
                        loaders.add(Loader.valueOf(s.trim().toLowerCase()));
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        System.out.println(BOLD + "Releasing..." + NO_NL);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        Map<Platform, List<File>> filesByPlatform = new HashMap<>();
        for (var entry : releaseFiles) {
            for (Platform p : entry.getValue()) {
                filesByPlatform.computeIfAbsent(p, k -> new ArrayList<>()).add(entry.getKey());
            }
        }

        if (filesByPlatform.containsKey(Platform.modrinth)) {
            futures.add(new ModrinthPlatform().release(config, filesByPlatform.get(Platform.modrinth), version, subtitle, description, channel, mcVersions, loaders));
        }
        if (filesByPlatform.containsKey(Platform.hangar)) {
            futures.add(new HangarPlatform().release(config, filesByPlatform.get(Platform.hangar), version, subtitle, description, channel, mcVersions, loaders));
        }
        if (filesByPlatform.containsKey(Platform.github)) {
            futures.add(new GithubPlatform().release(config, filesByPlatform.get(Platform.github), version, subtitle, description, channel, null, null));
        }

        if (!futures.isEmpty()) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }

    public static void config(Config config) {
        System.out.println(PURPLE + BOLD + "--- Configuring EasyRelease CLI ---" + RESET);

        System.out.print(CYAN + "Platforms (github, modrinth, hangar) [" + NO_NL + YELLOW + config.getPlatforms() + NO_NL + CYAN + "]: " + RESET);
        String platformsInput = sc.nextLine();
        List<Platform> selectedPlatforms = new ArrayList<>();
        for (String p : platformsInput.split(",")) {
            try {
                String platformName = p.trim().toLowerCase();
                selectedPlatforms.add(Platform.valueOf(platformName));
            } catch (Exception ignored) {
            }
        }
        config.setPlatforms(selectedPlatforms);

        if (config.getPlatforms().contains(Platform.github)) {
            System.out.print(CYAN + "GitHub Repo (owner/repo) [" + NO_NL + YELLOW + config.getGithubRepo() + NO_NL + CYAN + "]: " + RESET);
            String githubRepo = sc.nextLine();
            if (!githubRepo.isBlank()) config.setGithubRepo(githubRepo);

            System.out.print(CYAN + "GitHub Token [" + NO_NL + YELLOW + (config.getGithubToken() != null ? "****" : "null") + NO_NL + CYAN + "]: " + RESET);
            String githubToken = sc.nextLine();
            if (!githubToken.isBlank()) config.setGithubToken(githubToken);

            System.out.print(CYAN + "GitHub Main Branch [" + NO_NL + YELLOW + config.getMainBranch() + NO_NL + CYAN + "]: " + RESET);
            String mainBranch = sc.nextLine();
            if (!mainBranch.isBlank()) config.setMainBranch(mainBranch);
        }
        if (config.getPlatforms().contains(Platform.modrinth)) {
            System.out.print(CYAN + "Modrinth Project ID [" + NO_NL + YELLOW + config.getModrinthProjectId() + NO_NL + CYAN + "]: " + RESET);
            String modrinthId = sc.nextLine();
            if (!modrinthId.isBlank()) config.setModrinthProjectId(modrinthId);

            System.out.print(CYAN + "Modrinth Token [" + NO_NL + YELLOW + (config.getModrinthToken() != null ? "****" : "null") + NO_NL + CYAN + "]: " + RESET);
            String modrinthToken = sc.nextLine();
            if (!modrinthToken.isBlank()) config.setModrinthToken(modrinthToken);
        }

        if (config.getPlatforms().contains(Platform.hangar)) {
            System.out.print(CYAN + "Hangar Author [" + NO_NL + YELLOW + config.getHangarAuthor() + NO_NL + CYAN + "]: " + RESET);
            String author = sc.nextLine();
            if (!author.isBlank()) config.setHangarAuthor(author);

            System.out.print(CYAN + "Hangar Slug [" + NO_NL + YELLOW + config.getHangarSlug() + NO_NL + CYAN + "]: " + RESET);
            String slug = sc.nextLine();
            if (!slug.isBlank()) config.setHangarSlug(slug);

            System.out.print(CYAN + "Hangar API Key [" + NO_NL + YELLOW + (config.getHangarApiKey() != null ? "****" : "null") + NO_NL + CYAN + "]: " + RESET);
            String hangarApiKey = sc.nextLine();
            if (!hangarApiKey.isBlank()) config.setHangarApiKey(hangarApiKey);
        }

        if (config.getPlatforms().contains(Platform.hangar) || config.getPlatforms().contains(Platform.modrinth)) {
            if (config.getMinecraftVersions() == null || config.getMinecraftVersions().isEmpty()) {
                System.out.print(CYAN + "Default Minecraft Versions (comma separated) [" + NO_NL + YELLOW + config.getMinecraftVersions() + NO_NL + CYAN + "]: " + RESET);
                String mcVersions = sc.nextLine();
                if (!mcVersions.isBlank()) config.setMinecraftVersions(Arrays.asList(mcVersions.split(",")));
            }

            if (config.getLoaders() == null || config.getLoaders().isEmpty()) {
                System.out.print(CYAN + "Default Loaders (comma separated) [" + NO_NL + YELLOW + config.getLoaders() + NO_NL + CYAN + "]: " + RESET);
                String loaderInput = sc.nextLine();
                if (!loaderInput.isBlank()) {
                    List<Loader> loaders = new ArrayList<>();
                    for (String s : loaderInput.split(",")) {
                        try {
                            loaders.add(Loader.valueOf(s.trim().toLowerCase()));
                        } catch (Exception ignored) {
                        }
                    }
                    config.setLoaders(loaders);
                }
            }
            System.out.print(CYAN + "Dependencies (name:type:platform:url, comma separated) [" + NO_NL + YELLOW + config.getDependencies() + NO_NL + CYAN + "]: " + RESET);
            String depInput = sc.nextLine();
            if (!depInput.isBlank()) {
                List<Dependency> deps = new ArrayList<>();
                for (String s : depInput.split(",")) {
                    String[] parts = s.split(":");
                    if (parts.length >= 2) {
                        Dependency dep = new Dependency(parts[0].trim(), parts[1].trim());
                        if (parts.length >= 3 && !parts[2].isBlank()) {
                            dep.setHangarPlatform(parts[2].trim().toUpperCase());
                        }
                        if (parts.length >= 4 && !parts[3].isBlank()) {
                            dep.setExternalUrl(parts[3].trim());
                        }
                        deps.add(dep);
                    }
                }
                config.setDependencies(deps);
            }

        }

        ConfigManager.saveConfig(config);
        System.out.println(GREEN + "Configuration saved!" + RESET);
    }

    public static Channel getChannel(Set<Platform> selectedPlatforms) {
        String str = sc.nextLine();
        Channel channel = Channel.BasicChannel.fromString(str);
        if (channel == null && !selectedPlatforms.contains(Platform.hangar)) {
            System.out.print(RED + "Invalid channel! " + RESET + CYAN + "Try again (release, beta, alpha): " + RESET);
            return getChannel(selectedPlatforms);
        } else if (selectedPlatforms.contains(Platform.hangar)) {
            System.out.println("Channel does not match standards, but trying it out because hangar is enabled" + RESET);
            return () -> str;
        }

        return channel;
    }
}