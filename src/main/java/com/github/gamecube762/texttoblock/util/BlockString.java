package com.github.gamecube762.texttoblock.util;

import com.github.gamecube762.texttoblock.services.FontManager;
import com.github.gamecube762.texttoblock.services.TextToBlock;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.awt.*;
import java.util.*;

/**
 * BlockString; Your string, in the form of Blocks!
 */
public class BlockString {

    private Map<Character, BlockCharacter> bCharsStorage;//used to prevent holding duplicate chars
    private String text;
    private Font font;
    private Alignment alignment = Alignment.LEFT;

    public BlockString(String text) {
        this(text, null);
    }

    public BlockString(String text, Font font) {
        this(text, font, Alignment.LEFT);
    }

    public BlockString(String text, Font font, Alignment alignment) {
        if (font == null)
            font = FontManager.getMe().getDefaultFont().get();

        this.text = text;
        this.font = font;
        this.alignment = alignment;
        this.bCharsStorage = TextToBlock.getMe().getBlockCharactersMap(text, font);
    }

    /**
     * Pastes the BlockString at desired location.
     * Places from Bottom-Left
     *
     * @param location Location to paste at
     * @param blockType Block to use
     * @param cause Cause
     */
    public void pasteAt(Location<World> location, BlockType blockType, Cause cause) {
        int width = 0;
        int height = getHeight();

        for (int i = 0; i < asLines().length; i++) {
            String line = asLines()[i];
            height -= getLineHeight(i);
            switch (alignment) {
                default:
                case LEFT: width = 0; break;
                case CENTER: width = (getWidth()/2)-(getLineWidth(i)/2); break;
                case RIGHT: width = getWidth() - getLineWidth(i);break;
            }

            for (char c : line.toCharArray()){
                BlockCharacter b = bCharsStorage.get(c);
                b.pasteAt(location.add(width, height, 0), blockType, cause);
                width += b.getWidth();
            }
        }


    }

    //todo asSchematic

    /**
     * Get the ammount of lines in the string.
     *
     * @return the line count
     */
    public int getLineCount() {
        int a = 1;
        for (char b : asString().toCharArray())
            if (b == '\n')
                a +=1;
        return a;
    }

    /**
     * Returns the width of the text.
     *
     * This will be length in block of the text.
     *
     * @return width in blocks
     */
    public int getWidth() {
        int a = 0;
        for (String s : asLines()) {
            int b = 0;
            for (char c : s.toCharArray()) {
                if (c == '\n') continue;
                BlockCharacter d = bCharsStorage.get(c);
                b += d.getWidth();
            }
            if (b > a)
                a = b;
        }
        return a;
    }

    /**
     * Gets the total block width of the message as if it was one-lined text.
     *
     * @return width in blocks
     */
    public int getLineWidth(int line) {
        if (line < 0 || line >= getLineCount()) return -1;

        int a = 0;
        for (char c : asLines()[line].toCharArray()) {
            if (c == '\n') continue;
            BlockCharacter d = bCharsStorage.get(c);
            a += d.getWidth();
        }
        return a;
    }

    /**
     * Returns the longest height of all the lines in the string by counting the height of each character.
     *
     * This will be height in block of the text.
     *
     * @return height in blocks
     */
    public int getHeight() {
        int a = 0;
        for (String s : asLines()) {
            int b = 0;
            for (char c : s.toCharArray()) {
                if (c == '\n') continue;
                int d = bCharsStorage.get(c).getHeight();
                if (d > b)
                    b = d;
            }
            if (b > a)
                a = b;
        }
        return a;
    }

    /**
     * Get the max height of this line.
     * 0 index
     * returns -1 if line is not found.
     *
     * @param line line to get.
     * @return line height or -1
     */
    public int getLineHeight(int line) {
        if (line < 0 || line >= getLineCount()) return -1;

        int a = 0;
        for (char c : asLines()[line].toCharArray()) {
            if (c == '\n') continue;
            int h = bCharsStorage.get(c).getHeight();
            if (h > a)
                a = h;
        }
        return a;
    }

    /**
     * Get the String of the message
     *
     * @return the message as a string
     */
    public String asString() {
        return text;
    }

    /**
     * Assuming the string contains \n, this will return as an array of strings divided by each \n.
     *
     * @return String array of the lines from the message
     */
    public String[] asLines() {
        return asString().split("[\n]");
    }

    /**
     * Get the char array of the message.
     *
     * @return the character array of the message
     */
    public char[] asChars() {
        return asString().toCharArray();
    }

    /**
     * Get the message as an array of BlockCharacters.
     *
     * @return the message as an array of BlockCharacters
     */
    public BlockCharacter[] asBlockCharacters() {
        ArrayList<BlockCharacter> a = new ArrayList<>();
        for (char c : asString().toCharArray())
            a.add(bCharsStorage.get(c));
        return (BlockCharacter[]) a.toArray();
    }

    /**
     * Get the map that is used to store BlockCharacters for this BlockString.
     * The map will only contain characters used by the text.
     *
     * @return character storage map<Character, BlockCharacter>
     */
    public Map<Character, BlockCharacter> getbBlockCharacterStorage() {
        return bCharsStorage;
    }

    /**
     * Get the font used for this BlockString
     *
     * @return font
     */
    public Font getFont() {
        return font;
    }

    /**
     * Get the text alignment for this BlockString
     *
     * @return alignment
     */
    public Alignment getAlignment() {
        return alignment;
    }

    /**
     * Set the text alignment for this BlockString
     *
     * @param alignment Text alignment to use
     */
    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

}
