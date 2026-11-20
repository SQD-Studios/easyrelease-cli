package com.sqdstudios.easyrelease.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sqdstudios.easyrelease.cli.model.Config;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static File configFile = new File(".easyrelease-cli");

    public static void setConfigFile(File file) {
        configFile = file;
    }

    public static void initializeConfigPath(String[] args) {
        String customPath = System.getProperty("config");
        if (customPath != null) {
            configFile = new File(customPath);
        }
    }

    public static Config loadConfig() {
        if (!configFile.exists()) {
            return new Config();
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
