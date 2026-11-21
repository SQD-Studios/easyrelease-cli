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
            System.out.println(RESET + PURPLE + "You stopped the input, assuming you were to close the program" + RESET);
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

        System.out.print(CYAN + "File Path (e.g., build/libs/plugin.jar): " + RESET);
        String filePath = sc.nextLine();
        if (filePath.length() >= 2 && filePath.startsWith("\"") && filePath.endsWith("\"")) {
            filePath = filePath.substring(1, filePath.length() - 1);
        }
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println(RED + "File not found: " + file.getAbsolutePath() + RESET);
            System.exit(-3);
        }

        if (config.getPlatforms() == null || config.getPlatforms().isEmpty()) {
            System.out.print(CYAN + "Platforms to upload to (github, modrinth): " + RESET);
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
        }

        if (config.getPlatforms().isEmpty()) {
            System.out.println(YELLOW + "No valid platforms selected." + RESET);
            System.exit(-2);
        }

        Channel channel;
        if (config.getPlatforms().contains(Platform.modrinth)) {
            System.out.print(CYAN + "Channel (release, beta, alpha): " + RESET);
            channel = getChannel();
        } else {
            System.out.print(CYAN + "Prerelease (true/false): " + RESET);
            String prerelease = sc.nextLine();
            if (Boolean.getBoolean(prerelease)) channel = Channel.beta;
            else channel = Channel.release;
        }

        List<String> mcVersions = new ArrayList<>();
        List<Loader> loaders = new ArrayList<>();
        if (config.getPlatforms().contains(Platform.modrinth)) {
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

        System.out.println(BOLD + "Releasing..." + RESET);
        for (Platform platform : config.getPlatforms()) {
            if (platform == Platform.modrinth) {
                new ModrinthPlatform().release(config, file, version, subtitle, description, channel, mcVersions, loaders)
                        .whenComplete((_, _) -> {
                        });
            } else if (platform == Platform.github) {
                new GithubPlatform().release(config, file, version, subtitle, description, channel, mcVersions, loaders)
                        .whenComplete((_, _) -> {
                        });
            }
        }
    }

    public static void config(Config config) {
        System.out.println(PURPLE + BOLD + "--- Configuring EasyRelease CLI ---" + RESET);

        System.out.print(CYAN + "Platforms (github, modrinth) [" + NO_NL + YELLOW + config.getPlatforms() + NO_NL + CYAN + "]: " + RESET);
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

            System.out.print(CYAN + "GitHub Token [" + NO_NL + YELLOW + (config.getGithubToken(true) != null ? "****" : "null") + NO_NL + CYAN + "]: " + RESET);
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

            System.out.print(CYAN + "Modrinth Token [" + NO_NL + YELLOW + (config.getModrinthToken(true) != null ? "****" : "null") + NO_NL + CYAN + "]: " + RESET);
            String modrinthToken = sc.nextLine();
            if (!modrinthToken.isBlank()) config.setModrinthToken(modrinthToken);

            System.out.print(CYAN + "Default Minecraft Versions (comma separated) [" + NO_NL + YELLOW + config.getMinecraftVersions() + NO_NL + CYAN + "]: " + RESET);
            String mcVersions = sc.nextLine();
            if (!mcVersions.isBlank()) config.setMinecraftVersions(Arrays.asList(mcVersions.split(",")));

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

            System.out.print(CYAN + "Dependencies (slug:type, comma separated) [" + NO_NL + YELLOW + config.getDependencies() + NO_NL + CYAN + "]: " + RESET);
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