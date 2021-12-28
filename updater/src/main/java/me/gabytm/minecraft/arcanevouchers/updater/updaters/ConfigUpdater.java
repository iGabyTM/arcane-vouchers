package me.gabytm.minecraft.arcanevouchers.updater.updaters;

import me.gabytm.minecraft.arcanevouchers.updater.utils.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class ConfigUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUpdater.class);

    public ConfigUpdater() {
        final File configFile = new File("config.yml");

        if (!Files.checkFile(configFile)) {
            return;
        }

        try {
            Files.copy("config.yml");
            final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .file(configFile)
                    .headerMode(HeaderMode.PRESERVE)
                    .nodeStyle(NodeStyle.BLOCK)
                    .indent(2)
                    .build();
            final CommentedConfigurationNode config = loader.load();

            update(config);
            loader.save(config);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void update(final CommentedConfigurationNode config) throws SerializationException {
        config.node("settings").act(settings ->{
            settings.node("disabled", "crafting").set(settings.node("disabledCrafting").getBoolean());
            settings.removeChild("disabledCrafting");

            if (settings.node("command").empty()) {
                settings.node("command").set("arcanevouchers");
            }

            if (settings.node("alias").empty()) {
                settings.node("alias").set(Collections.singletonList("av"));
            }
        });

        config.node("confirmationGui", "items").act(items -> {
            items.node("buttons").act(buttons -> {
                for (final CommentedConfigurationNode button : buttons.childrenMap().values()) {
                    final String name = button.node("display_name").getString();

                    if (name != null) {
                        button.node("name").set(name);
                        button.removeChild("display_name");
                    }
                }
            });

            items.node("other").act(other -> {
                for (final CommentedConfigurationNode item : other.childrenMap().values()) {
                    final String name = item.node("display_name").getString();

                    if (name != null) {
                        item.node("name").set(name);
                        item.removeChild("display_name");
                    }
                }
            });
        });
    }

}
