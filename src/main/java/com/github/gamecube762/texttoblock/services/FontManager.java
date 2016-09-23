package com.github.gamecube762.texttoblock.services;

import ninja.leaping.configurate.ConfigurationNode;
import org.slf4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FontManager, a service that manages fonts.
 */
public interface FontManager {

    /**
     * Load fonts from font folder. All fonts will be in the FontCache. Use .getFont(...) to get your font.
     *
     * @param search Search extra dirs defined in config
     * @param errFiles Attempt to load Proxies that contains [ERROR]
     * @param loadAgain Load fonts even though they've already been loaded.
     */
    void loadFontFolder(boolean search, boolean errFiles, boolean loadAgain);

    /**
     * Creates a .ttfproxy from the specified .ttf file and saves it to the font folder.
     * Returns False if errors were thrown.
     *
     * @param file fileIn
     * @throws IOException
     */
    boolean createFontProxyFileNOERR(File file);

    /**
     * Creates a .ttfproxy from the specified .ttf file and saves it to the font folder.
     *
     * @param file fileIn
     * @throws IOException exception
     */
    void createFontProxyFile(File file) throws IOException;

    /**
     * Returns an optional of the desired font. Optional will be empty if no font is found.
     * Font will be default size from the config.
     *
     * @param name FontName
     * @return Optional of desired font
     */
    Optional<Font> getFont(String name);

    /**
     * Returns an optional of the desired font. Optional will be empty if no font is found.
     *
     * @param name FontName
     * @param size FontSize
     * @return Optional of desired font
     */
    Optional<Font> getFont(String name, float size);

    /**
     * Gets an optional of the default font that was defined in the config.
     * Will be empty only if no fonts were loaded.
     *
     * @return the default font defined in the config
     */
    Optional<Font> getDefaultFont();

    /**
     * Gets an optional of the default font that was defined in the config.
     * Will be empty only if no fonts were loaded.
     *
     * @param size desired font size
     * @return the default font defined in the config
     */
    Optional<Font> getDefaultFont(float size);

    /**
     * Returns an optional of the desired font or the default font if the desired font was not found.
     * Font will be default size from the config.
     *
     * @param name FontName
     * @return desired font or default font; either way you are getting a font.
     */
    Optional<Font> getFontOrDefault(String name);

    /**
     * Returns an optional of the desired font or the default font if the desired font was not found.
     *
     * @param name FontName
     * @param size FontSize
     * @return desired font or default font; either way you are getting a font.
     */
    Optional<Font> getFontOrDefault(String name, float size);

    /**
     * Get all the current loaded fonts.
     *
     * @return list of loaded fonts.
     */
    Collection<Font> getLoadedFonts();

    /**
     * Attempts to load font from specified file.
     * Returns empty if errors were thrown.
     *
     * @param file Font File to load
     * @return Optional of Font
     */
    Optional<Font> loadFontNOERR(File file);

    /**
     * Attempts to load font from specified file.
     *
     * @param file Font File to load
     * @return font if no exceptions were thrown
     * @throws IOException ~
     * @throws FontFormatException ~
     */
    Font loadFont(File file) throws IOException, FontFormatException;

}
