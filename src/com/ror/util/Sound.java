package com.ror.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import javax.sound.sampled.*;
import javax.swing.SwingUtilities;

public class Sound {

    public static void play(String path) {
        try {
            URL url = Sound.class.getResource(path);
            if (url == null) {
                System.err.println("Sound not found: " + path);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playThen(String path, Runnable after) {
        try {
            InputStream audioSrc = Sound.class.getResourceAsStream(path);
            if (audioSrc == null) {
                System.out.println("SFX not found: " + path);
                return;
            }

            BufferedInputStream buffered = new BufferedInputStream(audioSrc);
            AudioInputStream ais = AudioSystem.getAudioInputStream(buffered);

            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();

                    SwingUtilities.invokeLater(after);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
