package com.sqdstudios.easyrelease.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sqdstudios.easyrelease.cli.model.Config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

import static com.sqdstudios.easyrelease.cli.Main.config;

public class ConfigManager {
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static File configFile = resolveConfigPath().toFile();

    public static void setConfigFile(File file) {
        configFile = file;
    }

    static String shortHash(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) sb.append(String.format("%02x", d[i]));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Path resolveConfigPath() {
        Path home = Paths.get(System.getProperty("user.home"));
        Path cwd = Paths.get("").toAbsolutePath().normalize();

        return home.resolve(".easyrelease").resolve(shortHash(cwd.toString()) + ".json");
    }

    public static Config loadConfig() {
        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }
        if (!configFile.exists()) {
            Config config = new Config();
            config(config);
            return config;
        }
        try {
            return mapper.readValue(configFile, Config.class);
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
            return new Config();
        }
    }

    public static void saveConfig(Config config) {
        try {
            mapper.writeValue(configFile, config);
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }
}
