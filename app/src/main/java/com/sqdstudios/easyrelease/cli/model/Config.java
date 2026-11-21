package com.sqdstudios.easyrelease.cli.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private String modrinthProjectId;
    private String githubRepo; // owner/repo
    private String mainBranch;
    private List<String> minecraftVersions;
    private List<Loader> loaders;
    private List<Dependency> dependencies;
    private String modrinthToken;
    private String githubToken;
    private String hangarApiKey;
    private String hangarAuthor;
    private String hangarSlug;
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

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public String getModrinthToken() {
        return modrinthToken;
    }

    public void setModrinthToken(String modrinthToken) {
        this.modrinthToken = modrinthToken;
    }

    public String getGithubToken() {
        return githubToken;
    }

    public String getHangarApiKey() {
        return hangarApiKey;
    }

    public void setHangarApiKey(String hangarApiKey) {
        this.hangarApiKey = hangarApiKey;
    }

    public String getHangarAuthor() {
        return hangarAuthor;
    }

    public void setHangarAuthor(String hangarAuthor) {
        this.hangarAuthor = hangarAuthor;
    }

    public String getHangarSlug() {
        return hangarSlug;
    }

    public void setHangarSlug(String hangarSlug) {
        this.hangarSlug = hangarSlug;
    }

    @JsonIgnore
    public String getModrinthTokenEnv() {
        if (modrinthToken == null) return System.getenv("MODRINTH_TOKEN");
        return modrinthToken;
    }

    @JsonIgnore
    public String getGithubTokenEnv() {
        if (githubToken == null) return System.getenv("GITHUB_TOKEN");
        return githubToken;
    }

    @JsonIgnore
    public String getHangarApiKeyEnv() {
        if (hangarApiKey == null) return System.getenv("HANGAR_API_KEY");
        return hangarApiKey;
    }

    public void setGithubToken(String githubToken) {
        this.githubToken = githubToken;
    }
}
