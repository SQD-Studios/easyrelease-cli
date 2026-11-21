package com.sqdstudios.easyrelease.cli.model;

import com.sqdstudios.easyrelease.cli.platform.GithubPlatform;
import com.sqdstudios.easyrelease.cli.platform.HangarPlatform;
import com.sqdstudios.easyrelease.cli.platform.ModrinthPlatform;

public enum Platform {
    modrinth(ModrinthPlatform.class, "modrinth"),
    github(GithubPlatform.class, "github"),
    hangar(HangarPlatform.class, "hangar"),
    ;

    public final Class<? extends com.sqdstudios.easyrelease.cli.platform.Platform> publishClass;
    public final String name;

    Platform(Class<? extends com.sqdstudios.easyrelease.cli.platform.Platform> publishClass, String name) {
        this.publishClass = publishClass;
        this.name = name;
    }
}