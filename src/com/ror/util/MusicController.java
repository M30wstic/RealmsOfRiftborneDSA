package com.ror.util;

public class MusicController {

    private static String currentTrack = null;

    public static void play(String path, boolean loop) {
        if (path == null) return;

        if (path.equals(currentTrack)) {
            return; // already playing
        }

        currentTrack = path;
        Music.playOgg(path, loop);
    }

    public static void stop() {
        currentTrack = null;
        Music.stop();
    }
}
