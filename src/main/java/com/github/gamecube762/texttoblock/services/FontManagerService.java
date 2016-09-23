package com.github.gamecube762.texttoblock.services;

import ninja.leaping.configurate.ConfigurationNode;
import org.slf4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FontManager, a service that manages fonts.
 * This service is automatically started and registered by the plugin.
 *
 * Fonts will be loaded from "root/fonts/" and directories listed in the plugin config will be scanned for fonts.
 * .ttfproxy will be created for fonts found during the scan and saved to the fonts folder.
 *
 * ---
 *
 * .ttfproxy is a made-up file for this manager. It's a text file that contains the directory to the original ttf file.
 * This allows us to keep track of ttf files outside of the server directory.
 * Errors with loading the referenced .ttf will be saved into the ttfproxy file and the proxy will have "[ERROR]" at the start of it's name.
 */
public class FontManagerService implements FontManager {

    private static FontManagerService me;

    @Deprecated
    public static FontManagerService getMe() {
        return me;
    }

    public final Path FONTDIR = Paths.get("fonts");

    private final Map<String, Font> cache = new ConcurrentHashMap<>();
    private Logger logger;
    private String defaultFontName;
    private ConfigurationNode config;

    protected FontManagerService(){}//testing reasons

    public FontManagerService(Logger logger, ConfigurationNode config) {
        this.logger = logger;
        this.config = config;
        this.defaultFontName = config.getNode("defaults", "font").getString();

        loadFontFolder(true, true, false);

        if (getFont(defaultFontName).isPresent()) return;
        if (cache.isEmpty()){
            logger.warn("No Fonts Loaded! This may cause errors within the plugin!");
            defaultFontName = "";
            return;
        }

        StringBuilder sb = new StringBuilder(String.format("Font \"%s\" was not found.", defaultFontName));

        defaultFontName = getFont("Arial").isPresent() ? "Arial" : (String) cache.values().stream().map(Font::getName).sorted().toArray()[0];

        sb.append(String.format(" Using %s instead.", defaultFontName));
        logger.warn(sb.toString());
    }


    /**
     * Load fonts from font folder. All fonts will be in the FontCache. Use .getFont(...) to get your font.
     *
     * @param search Search extra dirs defined in config
     * @param errFiles Attempt to load Proxies that contains [ERROR]
     * @param loadAgain Load fonts even though they've already been loaded.
     */
    public void loadFontFolder(boolean search, boolean errFiles, boolean loadAgain) {
        if (!Files.exists(FONTDIR))
            try {Files.createDirectories(FONTDIR);}
            catch (IOException e) {
                logger.error(String.format("Failed to create Font directory! | IOExc: %s", e.getMessage()));
                return;
            }

        if (search)
            config.getNode("fontmanager", "extraScanDirs").getList(o -> (String) o).forEach(o -> {
                Path p = Paths.get(o);
                if (!Files.exists(p)) return;
                if (Files.isRegularFile(p)) createFontProxyFileNOERR(p);
                if (Files.isDirectory(p)) {
                    try (DirectoryStream<Path> paths = Files.newDirectoryStream(p, "*.ttf")) {
                        Iterator<Path> i = paths.iterator();

                        if (!i.hasNext()) {
                            logger.warn(o + " doesn't contain fonts.");
                            return;
                        }
                        while (i.hasNext())
                            createFontProxyFileNOERR(i.next());
                    }
                    catch (IOException | DirectoryIteratorException e ) {
                        logger.warn(String.format("Unable to scan \"%s\". %s: %s", o, e.getClass().getName(), e.getMessage()));
                    }
                }
            });

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(FONTDIR, errFiles ? "*.{ttf,ttfproxy}" : "*.ttf")) {
            Iterator<Path> i = paths.iterator();
            if (!i.hasNext()) {
                logger.warn("No Fonts Found!");
                return;
            }

            while (i.hasNext())
                loadFontNOERR(i.next());
        }
        catch (IOException | DirectoryIteratorException e ) {
            logger.warn(String.format("Unable to load font folder. %s: %s", e.getClass().getName(), e.getMessage()));
        }
    }

    /**
     * Creates a .ttfproxy from the specified .ttf file and saves it to the font folder.
     * Returns False if errors were thrown.
     *
     * @param path path to font
     */
    public boolean createFontProxyFileNOERR(Path path) {
        try {createFontProxyFile(path); return true;}
        catch (IOException | IllegalArgumentException ignore) {return false;}
    }

    /**
     * Creates a .ttfproxy from the specified .ttf file and saves it to the font folder.
     *
     * @param path path to font
     * @throws IOException If failed to write
     * @throws IllegalArgumentException If path is not to a .ttf file
     */
    public void createFontProxyFile(Path path) throws IOException, IllegalArgumentException {
        String name = path.getFileName().toString();
        if (!name.contains(".ttf"))
            throw new IllegalArgumentException("Unsupported Filetype: " + path.getFileName().toString());
        Files.write(FONTDIR.resolve(name + "proxy"), path.toAbsolutePath().toString().getBytes());
    }

    /**
     * Returns an optional of the desired font. Optional will be empty if no font is found.
     * Font will be default size from the config.
     *
     * @param name FontName
     * @return Optional of desired font
     */
    public Optional<Font> getFont(String name) {
        return getFont(name, config.getNode("defaults", "fontsize").getInt());
    }

    /**
     * Returns an optional of the desired font. Optional will be empty if no font is found.
     *
     * @param name FontName
     * @param size FontSize
     * @return Optional of desired font
     */
    public Optional<Font> getFont(String name, float size) {
        name = name.replace(' ', '_');
        for (Map.Entry<String, Font> entry : cache.entrySet())
            if (entry.getKey().replace(' ', '_').equalsIgnoreCase(name) || entry.getValue().getName().replace(' ', '_').equalsIgnoreCase(name))
                return Optional.of(entry.getValue().deriveFont(size));
        return Optional.empty();
    }

    /**
     * Gets an optional of the default font that was defined in the config.
     * Will be empty only if no fonts were loaded.
     *
     * @return the default font defined in the config
     */
    public Optional<Font> getDefaultFont() {
        return getDefaultFont(config.getNode("defaults", "fontsize").getInt());
    }

    /**
     * Gets an optional of the default font that was defined in the config.
     * Will be empty only if no fonts were loaded.
     *
     * @param size desired font size
     * @return the default font defined in the config
     */
    public Optional<Font> getDefaultFont(float size) {
        return getFont(defaultFontName, size);
    }

    /**
     * Returns an optional of the desired font or the default font if the desired font was not found.
     * Font will be default size from the config.
     *
     * @param name FontName
     * @return desired font or default font; either way you are getting a font.
     */
    public Optional<Font> getFontOrDefault(String name) {
        return getFontOrDefault(name, config.getNode("defaults", "fontsize").getInt());
    }

    /**
     * Returns an optional of the desired font or the default font if the desired font was not found.
     *
     * @param name FontName
     * @param size FontSize
     * @return desired font or default font; either way you are getting a font.
     */
    public Optional<Font> getFontOrDefault(String name, float size) {
        Optional<Font> o = getFont(name, size);
        return o.isPresent() ? o : getDefaultFont(size);
    }

    /**
     * Get all the current loaded fonts.
     *
     * @return list of loaded fonts.
     */
    public Collection<Font> getLoadedFonts() {
        return cache.values();
    }

    /**
     * Attempts to load font from specified file.
     * Returns empty if errors were thrown.
     *
     * @param path Font File to load
     * @return Optional of Font
     */
    public Optional<Font> loadFontNOERR(Path path)  {
        try {return Optional.of(loadFont(path));}
        catch (FontFormatException | IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Attempts to load font from specified file.
     *
     * @param path Font File to load
     * @return font if no exceptions were thrown
     * @throws IOException If file is unable to be loaded.
     * @throws FontFormatException Thrown by Font.createFont(...)
     * @throws IllegalArgumentException If file is not .ttf or .ttfproxy
     */
    public Font loadFont(Path path) throws IOException, FontFormatException, IllegalArgumentException {
        String name = path.getFileName().toString();
        boolean isProxied = name.endsWith(".ttfproxy");
        Path proxy = null;
        List<String> pLines = null;
        Font f;

        if (!name.endsWith(".ttf") && !isProxied)
            throw new IllegalArgumentException("Unsupported Filetype: " + name);

        if (isProxied) {
            proxy = path;
            pLines = Files.readAllLines(path);//throws
            path = Paths.get(pLines.get(0));
        }

        if ((f = cache.get(getFileNameWithoutType(path))) != null)
            return f;

        try {f = Font.createFont(Font.TRUETYPE_FONT, path.toFile());}
        catch (FontFormatException | IOException e) {
            logger.error(String.format("Unable to load %s | %s", name, e.getMessage()));

            if (isProxied) {
                try {
                    if (!proxy.getFileName().startsWith("[ERROR] ")) {//Rename proxy file.
                        Files.delete(proxy);
                        proxy = Paths.get(proxy.getParent().toString(), "[ERROR] " + proxy.getFileName());
                    }

                    Files.write(proxy, Arrays.asList(pLines.get(0), "", "[ERROR]", "Failed to load font.", e.getClass().getName(), e.getMessage()));
                }
                catch (IOException ignore){/*Was the HDD disconnected or something?*/}
            }

            throw e;
        }

        if (!cache.containsValue(f))
            cache.put(getFileNameWithoutType(path), f);

        if (isProxied && proxy.getFileName().toString().startsWith("[Error] "))//No error, loaded fine.
            try {
                Files.delete(proxy);
                proxy = Paths.get(proxy.getParent().toString(), proxy.getFileName().toString().replace("[ERROR] ", ""));
                Files.write(proxy, pLines.get(0).getBytes());
            }
            catch (IOException e) {
                logger.error(String.format("Unable to update proxy %s | %s", proxy.getFileName(), e.getMessage()));
            }

        return f;
    }

    /**
     * Gets the name of a file without the file extension.
     *
     * @param path path to file
     * @return String returning filename
     */
    public static String getFileNameWithoutType(Path path) {
        String name = path.getFileName().toString();
        return name.contains(".") ? name.substring(0, name.lastIndexOf(".")) : name;
    }

    /**
     * Testing test stuff
     * @param args *Pirate*
     * @throws Exception if there is one
     */
    public static void main(String args[]) throws Exception {
    }

}
