package com.sqdstudios.easyrelease.cli;

import com.sqdstudios.easyrelease.cli.model.Channel;
import com.sqdstudios.easyrelease.cli.model.Config;
import com.sqdstudios.easyrelease.cli.model.Loader;
import com.sqdstudios.easyrelease.cli.model.Platform;
import com.sqdstudios.easyrelease.cli.platform.GithubPlatform;
import com.sqdstudios.easyrelease.cli.platform.ModrinthPlatform;

import java.io.File;
import java.util.*;

public class Main {

    private static final Scanner sc = new Scanner(System.in);

    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String PURPLE = "\u001B[35m";

    static void main(String[] args) {
        ConfigManager.initializeConfigPath(args);
        Config config = ConfigManager.loadConfig();

        if (args.length > 0 && args[0].equals("config")) {
            config(config);
            System.exit(0);
        }

        System.out.println(PURPLE + BOLD + "--- EasyRelease CLI ---" + RESET);

        System.out.print(CYAN + "Tag/Version Number: " + RESET);
        String version = sc.nextLine();

        System.out.print(CYAN + "Version Subtitle: " + RESET);
        String subtitle = sc.nextLine();

        System.out.println(CYAN + "Description (Changelog) - Enter an empty line or '.' to finish:" + RESET);
        StringBuilder descriptionBuilder = new StringBuilder();
        while (true) {
            String line = sc.nextLine();
            if (line.isEmpty() || line.equals(".")) break;
            if (!descriptionBuilder.isEmpty()) descriptionBuilder.append("\n");
            descriptionBuilder.append(line);
        }
        String description = descriptionBuilder.toString();

        System.out.print(CYAN + "File Path (e.g., build/libs/plugin.jar): " + RESET);
        String filePath = sc.nextLine();
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println(RED + "File not found: " + filePath + RESET);
            System.exit(-3);
        }

        System.out.print(CYAN + "Platforms to upload to (github, modrinth): " + RESET);
        String platformsInput = sc.nextLine();
        List<Platform> selectedPlatforms = new ArrayList<>();
        for (String p : platformsInput.split(",")) {
            try {
                String platformName = p.trim().toLowerCase();
                selectedPlatforms.add(Platform.valueOf(platformName));
            } catch (Exception ignored) {}
        }

        if (selectedPlatforms.isEmpty()) {
            System.out.println(YELLOW + "No valid platforms selected." + RESET);
            System.exit(-2);
        }

        System.out.print(CYAN + "Channel (release, beta, alpha): " + RESET);
        Channel channel = getChannel();

        List<String> mcVersions = config.getMinecraftVersions();
        if (mcVersions == null || mcVersions.isEmpty()) {
            System.out.print(CYAN + "Minecraft Versions (comma separated): " + RESET);
            mcVersions = Arrays.asList(sc.nextLine().split(","));
        }

        List<Loader> loaders = config.getLoaders();
        if (loaders == null || loaders.isEmpty()) {
            System.out.print(CYAN + "Loaders (e.g., paper, fabric - comma separated): " + RESET);
            String[] loaderStrings = sc.nextLine().split(",");
            loaders = new ArrayList<>();
            for (String s : loaderStrings) {
                try {
                    loaders.add(Loader.valueOf(s.trim().toLowerCase()));
                } catch (Exception ignored) {}
            }
        }

        System.out.println(BOLD + "Releasing..." + RESET);
        for (Platform platform : selectedPlatforms) {
            try {
                if (platform == Platform.modrinth) {
                    new ModrinthPlatform().release(config, file, version, subtitle, description, channel, mcVersions, loaders);
                } else if (platform == Platform.github) {
                    new GithubPlatform().release(config, file, version, subtitle, description, channel, mcVersions, loaders);
                }
            } catch (Exception e) {
                System.err.println(RED + "Error releasing to " + platform + ": " + e.getMessage() + RESET);
                e.printStackTrace();
            }
        }
    }

    public static void config(Config config) {
        System.out.println(PURPLE + BOLD + "--- Configuring EasyRelease CLI ---" + RESET);
        
        System.out.print(CYAN + "GitHub Repo (owner/repo) [" + RESET + YELLOW + config.getGithubRepo() + RESET + CYAN + "]: " + RESET);
        String githubRepo = sc.nextLine();
        if (!githubRepo.isBlank()) config.setGithubRepo(githubRepo);

        System.out.print(CYAN + "GitHub Token [" + RESET + YELLOW + (config.getGithubToken() != null ? "****" : "null") + RESET + CYAN + "]: " + RESET);
        String githubToken = sc.nextLine();
        if (!githubToken.isBlank()) config.setGithubToken(githubToken);

        System.out.print(CYAN + "GitHub Main Branch [" + RESET + YELLOW + config.getMainBranch() + RESET + CYAN + "]: " + RESET);
        String mainBranch = sc.nextLine();
        if (!mainBranch.isBlank()) config.setMainBranch(mainBranch);

        System.out.print(CYAN + "Modrinth Project ID [" + RESET + YELLOW + config.getModrinthProjectId() + RESET + CYAN + "]: " + RESET);
        String modrinthId = sc.nextLine();
        if (!modrinthId.isBlank()) config.setModrinthProjectId(modrinthId);

        System.out.print(CYAN + "Modrinth Token [" + RESET + YELLOW + (config.getModrinthToken() != null ? "****" : "null") + RESET + CYAN + "]: " + RESET);
        String modrinthToken = sc.nextLine();
        if (!modrinthToken.isBlank()) config.setModrinthToken(modrinthToken);

        System.out.print(CYAN + "Default Minecraft Versions (comma separated) [" + RESET + YELLOW + config.getMinecraftVersions() + RESET + CYAN + "]: " + RESET);
        String mcVersions = sc.nextLine();
        if (!mcVersions.isBlank()) config.setMinecraftVersions(Arrays.asList(mcVersions.split(",")));

        System.out.print(CYAN + "Default Loaders (comma separated) [" + RESET + YELLOW + config.getLoaders() + RESET + CYAN + "]: " + RESET);
        String loaderInput = sc.nextLine();
        if (!loaderInput.isBlank()) {
            List<Loader> loaders = new ArrayList<>();
            for (String s : loaderInput.split(",")) {
                try {
                    loaders.add(Loader.valueOf(s.trim().toLowerCase()));
                } catch (Exception ignored) {}
            }
            config.setLoaders(loaders);
        }

        System.out.print(CYAN + "Dependencies (slug:type, comma separated) [" + RESET + YELLOW + config.getDependencies() + RESET + CYAN + "]: " + RESET);
        String depInput = sc.nextLine();
        if (!depInput.isBlank()) {
            Map<String, String> deps = new HashMap<>();
            for (String s : depInput.split(",")) {
                String[] parts = s.split(":");
                if (parts.length == 2) {
                    deps.put(parts[0].trim(), parts[1].trim());
                }
            }
            config.setDependencies(deps);
        }

        ConfigManager.saveConfig(config);
        System.out.println(GREEN + "Configuration saved!" + RESET);
    }

    public static Channel getChannel() {
        String str = sc.nextLine();
        Channel channel = Channel.fromString(str);
        if (channel == null) {
            System.out.print(RED + "Invalid channel! " + RESET + CYAN + "Try again (release, beta, alpha): " + RESET);
            return getChannel();
        }
        return channel;
    }
}