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

    public final File FONTDIR = new File("fonts");

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
        if (!FONTDIR.exists())
            FONTDIR.mkdir();

        if (search)
            config.getNode("fontmanager", "extraScanDirs").getList(o -> (String) o).forEach(o -> {
                File f = new File(o);
                if (!f.exists()) return;
                if (f.isFile()) createFontProxyFileNOERR(f);
                if (f.isDirectory()) {
                    File[] b = null;
                    if ( (b = f.listFiles((dir, name) -> name != null && name.contains(".ttf"))) != null )
                        Arrays.stream(b).parallel().forEach(this::createFontProxyFileNOERR);
                }
            });

        File[] files;
        if ((files = FONTDIR.listFiles((dir, name) -> name.toLowerCase().endsWith(".ttf") || (name.toLowerCase().endsWith("ttfproxy") && (errFiles || !name.startsWith("[ERROR]"))))) == null) {
            logger.warn("No Fonts Found!");
            return;
        }

        Arrays.stream(files).forEach(this::loadFontNOERR);//parallel never properly ends? End of method is reached, but doesn't continue after the end of the loop.
    }

    /**
     * Creates a .ttfproxy from the specified .ttf file and saves it to the font folder.
     * Returns False if errors were thrown.
     *
     * @param file fileIn
     * @throws IOException
     */
    public boolean createFontProxyFileNOERR(File file) {
        try {createFontProxyFile(file); return true;}
        catch (IOException ignore) {return false;}
    }

    /**
     * Creates a .ttfproxy from the specified .ttf file and saves it to the font folder.
     *
     * @param file fileIn
     * @throws IOException exception
     */
    public void createFontProxyFile(File file) throws IOException {
        if (!file.getName().contains(".ttf"))
            throw new IOException("Unsupported Filetype: " + file.getName());
        Files.write(new File(FONTDIR, file.getName() + "proxy").toPath(), file.getAbsolutePath().getBytes());
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
     * @param file Font File to load
     * @return Optional of Font
     */
    public Optional<Font> loadFontNOERR(File file)  {
        try {return Optional.of(loadFont(file));}
        catch (FontFormatException | IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Attempts to load font from specified file.
     *
     * @param file Font File to load
     * @return font if no exceptions were thrown
     * @throws IOException ~
     * @throws FontFormatException ~
     */
    public Font loadFont(File file) throws IOException, FontFormatException {
        if (!file.getName().contains(".ttf") && !file.getName().contains(".ttfproxy"))
            throw new IOException("Unsupported Filetype: " + file.getName());

        boolean isProxied = file.getName().endsWith("ttfproxy");
        Path proxy = null;
        List<String> pLines = null;
        Font f = null;

        if (isProxied) {
            proxy = file.toPath();
            pLines = Files.readAllLines(file.toPath());//throws
            file = new File(pLines.get(0));
        }

        if ((f = cache.get(getFileNameWithoutType(file))) != null)
            return f;

        try {f = Font.createFont(Font.TRUETYPE_FONT, file);}
        catch (FontFormatException | IOException e) {
            logger.error(String.format("Unable to load %s | %s", file.getName(), e.getMessage()));

            if (isProxied) {
                try {
                    if (!proxy.getFileName().startsWith("[ERROR]")) {//Rename proxy file.
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
            cache.put(getFileNameWithoutType(file), f);

        if (isProxied && proxy.getFileName().toString().startsWith("[Error] "))//No more error, loaded fine.
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
     * @param file fileIn
     * @return String returning filename
     */
    public static String getFileNameWithoutType(File file) {
        String name = file.getName();
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
