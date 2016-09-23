package com.github.gamecube762.texttoblock.services;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

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
     * @param path fileIn
     */
    boolean createFontProxyFileNOERR(Path path);

    /**
     * Creates a .ttfproxy from the specified .ttf file and saves it to the font folder.
     *
     * @param path fileIn
     * @throws IOException exception
     * @throws IllegalArgumentException If path is not to a .ttf file
     */
    void createFontProxyFile(Path path) throws IOException, IllegalArgumentException;

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
     * @param path Font File to load
     * @return Optional of Font
     */
    Optional<Font> loadFontNOERR(Path path);

    /**
     * Attempts to load font from specified file.
     *
     * @param path Font File to load
     * @return font if no exceptions were thrown
     * @throws IOException If file is unable to be loaded.
     * @throws FontFormatException Thrown by Font.createFont(...)
     * @throws IllegalArgumentException If file is not .ttf or .ttfproxy
     */
    Font loadFont(Path path) throws IOException, FontFormatException, IllegalArgumentException;

}
