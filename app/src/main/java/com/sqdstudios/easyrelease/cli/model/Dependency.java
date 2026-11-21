package com.sqdstudios.easyrelease.cli.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Dependency {
    private String name; // Modrinth Project ID or Hangar Slug
    private String type; // required, optional, etc.
    private String hangarPlatform; // PAPER, VELOCITY, WATERFALL (optional, if null, applies to all)
    private String externalUrl; // For Hangar external dependencies

    public Dependency() {}

    public Dependency(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHangarPlatform() {
        return hangarPlatform;
    }

    public void setHangarPlatform(String hangarPlatform) {
        this.hangarPlatform = hangarPlatform;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public boolean isRequired() {
        return "required".equalsIgnoreCase(type);
    }
}