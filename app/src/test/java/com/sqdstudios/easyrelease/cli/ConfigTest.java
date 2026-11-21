package com.sqdstudios.easyrelease.cli;

import com.sqdstudios.easyrelease.cli.model.Config;
import com.sqdstudios.easyrelease.cli.model.Dependency;
import com.sqdstudios.easyrelease.cli.model.Loader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigTest {

    @Test
    void testConfigSerialization() {
        Config config = new Config();
        config.setModrinthProjectId("test-project");
        config.setGithubRepo("owner/repo");
        config.setMinecraftVersions(List.of("1.20.1", "1.20.2"));
        config.setLoaders(List.of(Loader.fabric, Loader.forge));
        config.setDependencies(List.of(
                new Dependency("fabric-api", "required"),
                new Dependency("modmenu", "optional")
        ));

        assertEquals("test-project", config.getModrinthProjectId());
        assertEquals("owner/repo", config.getGithubRepo());
        assertEquals(2, config.getMinecraftVersions().size());
        assertEquals(2, config.getLoaders().size());
        assertEquals(2, config.getDependencies().size());
        assertEquals("required", config.getDependencies().get(0).getType());
        assertEquals("optional", config.getDependencies().get(1).getType());
    }
}