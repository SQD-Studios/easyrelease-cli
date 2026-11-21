package com.sqdstudios.easyrelease.cli.platform;

import com.sqdstudios.easyrelease.cli.model.Channel;
import com.sqdstudios.easyrelease.cli.model.Config;
import com.sqdstudios.easyrelease.cli.model.Loader;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Platform {
    CompletableFuture<Void> release(
            Config config,
            List<File> files,
            String version,
            String subtitle,
            String description,
            Channel channel,
            List<String> mcVersions,
            List<Loader> loaders
    );
}