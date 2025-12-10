package com.ror.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

public class GameFonts {

    public static Font pixelFont;

    static {
        try {
            pixelFont = Font.createFont(
                Font.TRUETYPE_FONT,
                GameFonts.class.getResourceAsStream(
                    "/com/ror/model/assets/fonts/bytebounce.medium.ttf"
                )
            ).deriveFont(18f); 

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(pixelFont);

        } catch (Exception e) {
            e.printStackTrace();
            // in case font fails (hopefully not)
            pixelFont = new Font("Monospaced", Font.PLAIN, 18);
        }
    }


    private GameFonts() {}
}
