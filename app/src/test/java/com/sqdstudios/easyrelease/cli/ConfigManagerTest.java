package com.sqdstudios.easyrelease.cli;

import com.sqdstudios.easyrelease.cli.model.Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void testSaveAndLoadConfig() {
        File tempFile = tempDir.resolve(".easyrelease-cli-test").toFile();
        ConfigManager.setConfigFile(tempFile);

        Config config = new Config();
        config.setModrinthProjectId("test-project");
        config.setDependencies(Map.of("fabric-api", "required"));

        ConfigManager.saveConfig(config);

        assertTrue(tempFile.exists());

        Config loaded = ConfigManager.loadConfig();
        assertEquals("test-project", loaded.getModrinthProjectId());
        assertNotNull(loaded.getDependencies());
        assertEquals("required", loaded.getDependencies().get("fabric-api"));
    }
}
