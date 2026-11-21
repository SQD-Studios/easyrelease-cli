package com.sqdstudios.easyrelease.cli.model;

import com.sqdstudios.easyrelease.cli.platform.GithubPlatform;
import com.sqdstudios.easyrelease.cli.platform.HangarPlatform;
import com.sqdstudios.easyrelease.cli.platform.ModrinthPlatform;

public enum Platform {
    modrinth(ModrinthPlatform.class),
    github(GithubPlatform.class),
    hangar(HangarPlatform.class),
    ;

    public final Class<? extends com.sqdstudios.easyrelease.cli.platform.Platform> publishClass;

    Platform(Class<? extends com.sqdstudios.easyrelease.cli.platform.Platform> publishClass) {
        this.publishClass = publishClass;
    }
}