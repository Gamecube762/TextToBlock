package com.github.gamecube762.texttoblock.util;

import com.flowpowered.math.vector.Vector2d;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a character based on the inputted font/character.
 */
public class BlockCharacter {

    public static final BlockCharacter NEWLINE = new BlockCharacter('\n', 0, 0, null, new ArrayList<>());

    private char character;
    private int width, height;
    private Font font;
    private List<Vector2d> blockMap;

    /*
     * Since we can do new BlockString("Waffles"), I feel we should be able to do the same with BlockCharacters.
     * But as always there are complications..
     * I'd either have to duplicate ttb.toBlockCharacter another time or create a chardata class...
     * CharData is redundant as that what BlockCharacter is...
     */

    /**
     * Deprecated to discourage users from using this. Use TextToBlock#toBlockCharacter(...)
     *
     * @param character c
     * @param width w
     * @param height h
     * @param font f
     * @param blockMap b
     */
    @Deprecated
    public BlockCharacter(char character, int width, int height, Font font, List<Vector2d> blockMap) {
        this.character = character;
        this.width = width;
        this.height = height;
        this.font = font;
        this.blockMap = blockMap;
    }

    /**
     * Pastes this BlockCharacter at desired location.
     * Places from Bottom-Left
     *
     * @param location Location to paste at
     * @param blockType Block to use
     * @param cause Cause
     */
    public void pasteAt(Location<World> location, BlockType blockType, Cause cause) {
        blockMap.forEach(a ->
                location.add(a.getX(), a.getY(), 0).setBlockType(blockType, cause)
        );
    }

    /**
     * Get the character this was based off of.
     *
     * @return character
     */
    public char asCharacter() {
        return character;
    }

    /**
     * Get the size of the font used;
     *
     * @return FontSize
     */
    public int getFontSize() {
        return font.getSize();
    }

    /**
     * Get the width of this character.
     *
     * @return width as int
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the height of this character.
     *
     * @return height as int
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the font used to make this character.
     * @return font
     */
    public Font getFont() {
        return font;
    }

    /**
     * Get the map for block positions that are used to shape this character.
     *
     * @return List of 2D block positions
     */
    public List<Vector2d> getBlockMap() {
        return blockMap;
    }
}
