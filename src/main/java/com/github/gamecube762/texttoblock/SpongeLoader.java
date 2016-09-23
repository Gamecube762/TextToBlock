package com.github.gamecube762.texttoblock;

import com.github.gamecube762.texttoblock.services.FontManager;
import com.github.gamecube762.texttoblock.services.FontManagerService;
import com.github.gamecube762.texttoblock.services.TextToBlock;
import com.github.gamecube762.texttoblock.services.TextToBlockService;
import com.github.gamecube762.texttoblock.util.Alignment;
import com.github.gamecube762.texttoblock.util.BlockString;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Gamecube762 on 9/3/2016.
 */
@Plugin(id = "texttoblock", name = "TextToBlock", version = "0.0.2", description = "Turns text into blocks")
public class SpongeLoader {

    @Inject
    protected Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path configPath;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private ConfigurationNode rootNode;

    @Inject
    private PluginContainer container;

    private FontManagerService fontManager;
    private TextToBlockService textToBlock;

    @Listener
    public void gameInitialization(GamePreInitializationEvent event) {

        loadconfig();

        fontManager = new FontManagerService(logger, rootNode);
        textToBlock = new TextToBlockService(fontManager);

        Sponge.getServiceManager().setProvider(this, FontManager.class, fontManager);
        Sponge.getServiceManager().setProvider(this, TextToBlock.class, textToBlock);
        Sponge.getCommandManager().register(
                this,
                CommandSpec.builder()
                        .description(Text.of("List of loaded fonts"))
                        .executor((source, context) -> {
                            Collection<Font> fonts = fontManager.getLoadedFonts();

                            if (fonts.isEmpty())
                                source.sendMessage(Text.of("Fonts: None."));
                            else
                                source.sendMessage(Text.of(fonts.stream().map(Font::getName).sorted().collect(Collectors.joining(", ", String.format("Fonts(%s): ", fonts.size()), "."))));

                            return CommandResult.builder().queryResult(fonts.size()).build();
                        })
                        .build(),
                "fonts",
                "loadedfonts"
        );
        Sponge.getCommandManager().register(
                this,
                CommandSpec.builder()
                        .description(Text.of("Text to blocks"))
                        .permission("ttb.command")
                        .arguments(
                                GenericArguments.string(Text.of("f")),
                                GenericArguments.integer(Text.of("s")),
                                GenericArguments.string(Text.of("alignment")),
                                GenericArguments.remainingJoinedStrings(Text.of("message"))// "\\n" for "\n". The first\ seems to be stripped from commands.
                        )
                        .executor((source, context) -> {
                            Font f;
                            Optional<Font> fo = fontManager.getFontOrDefault(
                                    context.<String>getOne("f").get(),
                                    context.<Integer>getOne("s").get()
                            );
                            if (!fo.isPresent()) {
                                source.sendMessage(Text.of("Unknown font."));
                                return CommandResult.success();
                            }

                            f = fo.get();

                            new BlockString(
                                    context.<String>getOne("message").orElse("The quick brown fox\n jumps over\n the lazy dog.").replace("\\n", "\n"),
                                    f,
                                    Alignment.of(context.<String>getOne("alignment").orElse("center"))
                            ).pasteAt(
                                    ((Player)source).getLocation(),
                                    BlockTypes.DIAMOND_BLOCK,
                                    Cause.of(NamedCause.of("Plugin", container), NamedCause.simulated(source))
                            );
                            return CommandResult.success();
                        })
                        .build(),
                "ttb",
                "texttoblock"
        );

    }

    //
    //Config stuff
    //
    private HashMap<String, Object> defaultConfigMap;
    private boolean shouldSaveConf = false;

    private void loadconfig() {
        boolean a = true;
        if (defaultConfigMap == null) loadDefaultConfigMap();
        if (Files.exists(configPath))
            try {rootNode = configLoader.load();}
            catch (IOException ex) {
                a = false;
                logger.warn(String.format("Unable to load config file, using defaults. | %s", ex.getMessage()));
            }
        if (rootNode == null)
            rootNode = configLoader.createEmptyNode(ConfigurationOptions.defaults());

        defaultConfigMap.forEach((k, v) -> confAdd (v, k.split ("[.]")));

        if (a&shouldSaveConf)
            try {
                configLoader.save(rootNode);
                shouldSaveConf = false;
            }
            catch (IOException ex) {
                logger.warn(String.format("Unable to save config file. | %s", ex.getMessage()));
            }
    }

    private void confAdd(Object v, String... k) {
        ConfigurationNode node = rootNode.getNode(k);
        if (node.getValue() != null) return;
        node.setValue(v);
        shouldSaveConf = true;
    }

    private void loadDefaultConfigMap() {
        defaultConfigMap = new HashMap<>();
        defaultConfigMap.put("defaults.font", "arial");//todo find MC's font
        defaultConfigMap.put("defaults.fontsize", 16);
        defaultConfigMap.put("defaults.alignment", "left");

        defaultConfigMap.put("fontmanager.extraScanDirs", Arrays.asList("C:\\Windows\\Fonts"));
    }

}