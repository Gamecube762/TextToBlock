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
public interface TextToBlock {

    /**
     * Returns a map of BlockChars that the string uses.
     *
     * @param text Text to use
     * @param font Desired font
     * @return a map of BlockChars that the string uses
     */
    Map<Character, BlockCharacter> getBlockCharactersMap(String text, Font font);

    /**
     * You can just use new BlockString(...)
     *
     * @param text Text to be used
     * @param font Desired font
     * @return BlockString from Desired text and font
     */
    BlockString toBlockString(String text, Font font);

    /**
     * Converts a character into a BlockChar with the desired font
     *
     * For this to work, we draw the character onto an image and scan through the pixels to create a map of pixel positions. With these positions, we are able to place blocks in the same locations in order to create the text.
     *
     * @param c character
     * @param f desired font to use
     * @return Waffles! (It's actually the BlockChar)
     */
    BlockCharacter toBlockCharacter(char c, Font f);

    /**
     * Returns a list of positions for block placement that shapes out the inputted text.
     *
     * @param text input text
     * @param font desired font
     * @return list of 2d positions
     */
    List<Vector2d> getBlockPositions(String text, Font font) ;

}
