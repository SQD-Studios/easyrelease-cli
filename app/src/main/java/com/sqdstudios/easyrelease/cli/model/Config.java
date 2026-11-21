package com.sqdstudios.easyrelease.cli.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private String modrinthProjectId;
    private String githubRepo; // owner/repo
    private String mainBranch;
    private List<String> minecraftVersions;
    private List<Loader> loaders;
    private Map<String, String> dependencies; // slug -> dependency_type (required, optional, etc)
    private String modrinthToken;
    private String githubToken;
    private List<Platform> platforms;

    public List<Platform> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<Platform> platforms) {
        this.platforms = platforms;
    }

    public String getModrinthProjectId() {
        return modrinthProjectId;
    }

    public void setModrinthProjectId(String modrinthProjectId) {
        this.modrinthProjectId = modrinthProjectId;
    }

    public String getGithubRepo() {
        return githubRepo;
    }

    public void setGithubRepo(String githubRepo) {
        this.githubRepo = githubRepo;
    }

    public String getMainBranch() {
        return mainBranch;
    }

    public void setMainBranch(String mainBranch) {
        this.mainBranch = mainBranch;
    }

    public List<String> getMinecraftVersions() {
        return minecraftVersions;
    }

    public void setMinecraftVersions(List<String> minecraftVersions) {
        this.minecraftVersions = minecraftVersions;
    }

    public List<Loader> getLoaders() {
        return loaders;
    }

    public void setLoaders(List<Loader> loaders) {
        this.loaders = loaders;
    }

    public Map<String, String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Map<String, String> dependencies) {
        this.dependencies = dependencies;
    }

    public String getModrinthToken() {
        if (modrinthToken == null) return System.getenv("MODRINTH_TOKEN");
        return modrinthToken;
    }

    public void setModrinthToken(String modrinthToken) {
        this.modrinthToken = modrinthToken;
    }

    public String getGithubToken() {
        if (githubToken == null) return System.getenv("GITHUB_TOKEN");
        return githubToken;
    }

    public void setGithubToken(String githubToken) {
        this.githubToken = githubToken;
    }
}
