package com.sqdstudios.easyrelease.cli.model;

public enum Loader {
    // Plugin Based
    paper("Paper"),
    purpur("Purpur"),
    folia("Folia"),
    bungeecord("Bungeecord"),
    velocity("Velocity"),
    spigot("Spigot"),
    bukkit("Bukkit"),
    sponge("Sponge"),
    waterfall("Waterfall"),

    // Mod Based
    fabric("Fabric"),
    neoforge("Neoforge"),
    forge("Forge"),
    quilt("Quilt"),
    liteloader("Liteloader"),
    rift("Rift"),
    ornithe("Ornithe"),
    nilloader("Nilloader"),
    babric("Babric"),
    bta("BtaBabric"),
    legacy_fabric("LegacyFabric"),
    java_agent("JavaAgent"),
    risugamis_modloader("Modloader"),

    // Packs
    data_pack("Datapack"),
    resource_pack("Minecraft"),

    // Shaders
    optifine("Optifine"),
    iris("Iris"),
    canvas("Canvas"),
    vanilla_shader("Vanilla"),
    ;

    private final String modrinthType;

    Loader(String modrinthType) {
        this.modrinthType = modrinthType;
    }

    public String getModrinthType() {
        return modrinthType;
    }

    @Override
    public String toString() {
        return name().replace('_', '-');
    }
}