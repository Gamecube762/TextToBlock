package com.github.gamecube762.texttoblock.services;

import com.flowpowered.math.vector.Vector2d;
import com.github.gamecube762.texttoblock.util.Alignment;
import com.github.gamecube762.texttoblock.util.BlockCharacter;
import com.github.gamecube762.texttoblock.util.BlockString;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TextToBlock, a service used for creating BlockStrings and BlockCharacters.
 */
public class TextToBlock {

    //Map<Map<String, Character>, BlockChar> charCache
    //<<font, char>, blockchar>
    //Should we be caching them?

    private static TextToBlock me;
    /**
     * Deprecated as it's a workaround for the BlockChar and BlockString classes. Not sure if I should keep this.
     *
     * @return this
     */
    @Deprecated
    public static TextToBlock getMe() {
        return me;
    }

    private FontManager fontManager;

    /**
     * I'd suggest not making a new one. Why would you even need new one? Something wrong with the current one that you'd want a new one? Or do you like to so much that you require more?
     *
     * @param fontManager Pancakes Delivery Service.
     */
    public TextToBlock(FontManager fontManager) {
        if (me == null) me = this;
        this.fontManager = fontManager;
    }

    /**
     * Returns a map of BlockChars that the string uses.
     *
     * @param text Text to use
     * @param font Desired font
     * @return a map of BlockChars that the string uses
     */
    public Map<Character, BlockCharacter> getBlockCharactersMap(String text, Font font) {
        Map<Character, BlockCharacter> a = new HashMap<>();
        for (char c : text.toCharArray())//No stream/collection here. Darn char[] being primitive.
            if (!a.containsKey(c))
                a.put(c, toBlockCharacter(c, font));
        return a;
    }

    /**
     * You can just use new BlockString(...)
     *
     * @param text Text to be used
     * @param font Desired font
     * @return BlockString from Desired text and font
     */
    public BlockString toBlockString(String text, Font font) {
        return new BlockString(text, font);
    }

    /**
     * Converts a character into a BlockChar with the desired font
     *
     * For this to work, we draw the character onto an image and scan through the pixels to create a map of pixel positions. With these positions, we are able to place blocks in the same locations in order to create the text.
     *
     * @param c character
     * @param f desired font to use
     * @return Waffles! (It's actually the BlockChar)
     */
    public BlockCharacter toBlockCharacter(char c, Font f) {
        if (c == '\n') return BlockCharacter.NEWLINE;

        FontMetrics metrics = new JLabel().getFontMetrics(f);//From https://coderanch.com/t/465612/GUI/java/Extracting-Pixel-Data-Fonts
        int width = metrics.stringWidth(c + "");
        int height = metrics.getMaxAscent();
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.setFont(f);
        g2d.setColor(Color.black);
        g2d.drawString(c + "", 0, height);
        g2d.dispose();
        List<Vector2d> pos = new ArrayList<>();
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                if (bi.getRGB(x, y) != 0)
                    pos.add(new Vector2d(x, height-1-y));//Images 0,0 is at top-left; ours is from bot-left | Fixes upside-down text: https://www.youtube.com/watch?v=efjBHffmWRM
        return new BlockCharacter(c, width, height, f, pos);
    }

    /**
     * Returns a list of positions for block placement that shapes out the inputted text.
     *
     * @param text input text
     * @param font desired font
     * @return list of 2d positions
     */
    public List<Vector2d> getBlockPositions(String text, Font font) {
        /*
         * I didn't want to duplicate this, but it's the current work around..
         *
         * -BlockChar uses the width and height values from toBlockChar()
         * -BlockChar.CharData seems redundant as BlockChar should be it's own data
         * -BlockString has getBlockCharactersMap() here, having BlockCharacter's alt here seems more natural.
         * -'return toBlockCharacter(...).getBlockMap()' would require toBlockChar(...) take a string instead.
         * -toBlockChar(String, font) doesn't seem right..
         */
        FontMetrics metrics = new JLabel().getFontMetrics(font);
        int width = metrics.stringWidth(text);
        int height = metrics.getMaxAscent();
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.setFont(font);
        g2d.setColor(Color.black);
        g2d.drawString(text, 0, height);
        g2d.dispose();
        List<Vector2d> pos = new ArrayList<>();
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                if (bi.getRGB(x, y) != 0)
                    pos.add(new Vector2d(x, height-1-y));
        return pos;
    }

    /**
     * Testing test stuff
     * @param args *Pirate*
     * @throws Exception if there is one
     */
    public static void main(String[] args) throws Exception {
        FontMetrics metrics = new JLabel().getFontMetrics(new Font("Arial", Font.TRUETYPE_FONT, 16));//From https://coderanch.com/t/465612/GUI/java/Extracting-Pixel-Data-Fonts
        int width = metrics.stringWidth("C");
        int height = metrics.getMaxAscent();
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.setFont(new Font("Arial", Font.TRUETYPE_FONT, 16));
        g2d.setColor(Color.black);
        g2d.drawString("C", 0, height);
        g2d.dispose();

        ImageIO.write(bi, "png", new File("A:\\a.png"));//save to wam drive

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                if (bi.getRGB(x, y) != 0)
                    System.out.println(String.format("(%s, %s)", x, height-1-y));

        FontManager f = new FontManager();
        f.loadFontFolder(false,false,true);
        new TextToBlock(f);
        BlockString a = new BlockString("The quick brown fox\n jumps over\n the lazy dog.", f.getFont("Arial", 16).get(), Alignment.CENTER);
        System.out.println(a.getLineCount());
        System.out.println(a.getHeight());
        System.out.println(a.getWidth());

    }

}
