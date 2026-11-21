package com.sqdstudios.easyrelease.cli.model;

import com.sqdstudios.easyrelease.cli.platform.GithubPlatform;
import com.sqdstudios.easyrelease.cli.platform.ModrinthPlatform;

public enum Platform {
    modrinth(ModrinthPlatform.class),
    github(GithubPlatform.class),
    ;

    public final Class<?> classz;

    Platform(Class<?> classz) {
        this.classz = classz;
    }
}