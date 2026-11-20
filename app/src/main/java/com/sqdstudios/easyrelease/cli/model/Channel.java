package com.sqdstudios.easyrelease.cli.model;

public enum Channel {
    release,
    beta,
    alpha;

    public static Channel fromString(String channel) {
        return switch (channel.toLowerCase()) {
            case "release" -> release;
            case "beta" -> beta;
            case "alpha" -> alpha;
            default -> null;
        };
    }
}
