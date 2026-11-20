package com.sqdstudios.easyrelease.cli.model;

public enum Loader {
    // Plugin Based
    paper,
    purpur,
    folia,
    bungeecord,
    velocity,
    spigot,
    bukkit,
    sponge,
    waterfall,

    // Mod Based
    fabric,
    neoforge,
    forge,
    quilt,
    liteloader,
    rift,
    ornithe,
    nilloader,
    babric,
    bta,
    legacy_fabric,
    java_agent,
    risugamis_modloader,

    // Packs
    data_pack,
    resource_pack,

    // Shaders
    optifine,
    iris,
    canvas,
    vanilla_shader;

    @Override
    public String toString() {
        return name().replace('_', '-');
    }
}