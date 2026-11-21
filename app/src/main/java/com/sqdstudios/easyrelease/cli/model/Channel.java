package com.sqdstudios.easyrelease.cli.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Channel {

    List<Channel> channels = new ArrayList<>(Arrays.stream(BasicChannel.values()).toList());

    String name();

    static void addChannel(Channel channel) {
        channels.add(channel);
    }

    static List<Channel> getChannels() {
        return channels;
    }

    enum BasicChannel implements Channel {
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
}