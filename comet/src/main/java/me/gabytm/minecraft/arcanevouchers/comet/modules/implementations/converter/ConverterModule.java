package me.gabytm.minecraft.arcanevouchers.comet.modules.implementations.converter;

import me.gabytm.minecraft.arcanevouchers.comet.modules.CometModule;
import me.gabytm.minecraft.arcanevouchers.comet.modules.ModuleInfo;
import me.gabytm.minecraft.arcanevouchers.comet.modules.implementations.converter.converters.BadBonesVouchersConverter;
import me.gabytm.minecraft.arcanevouchers.comet.modules.implementations.converter.converters.SongodaVouchersConverter;
import org.apache.commons.cli.CommandLine;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

@ModuleInfo(id = "converter")
public class ConverterModule extends CometModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterModule.class);

    @SuppressWarnings("SpellCheckingInspection")
    public static List<String> converters() {
        return Arrays.asList("badbones", "songoda");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void create(@NotNull final File file) {
        if (file.exists()) {
            return;
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            LOGGER.error("Could not create " + file, e);
            System.exit(0);
        }
    }

    @Override
    public void run(CommandLine commandLine) {
        if (!commandLine.hasOption('p')) {
            LOGGER.warn("Plugin name not specified, add the '-p <{}>' argument", String.join(", ", converters()));
            System.exit(0);
        }

        final String pluginName = commandLine.getOptionValue('p');
        Converter converter;

        switch (pluginName.toLowerCase()) {
            case "badbones": {
                converter = new BadBonesVouchersConverter();
                break;
            }

            case "songoda": {
                converter = new SongodaVouchersConverter();
                break;
            }

            default: {
                LOGGER.warn("Unknown plugin {}", pluginName);
                System.exit(0);
                return;
            }
        }

        final File sourceFile = new File(converter.getFilePath());

        if (!sourceFile.isFile()) {
            LOGGER.error("{} is not a file", sourceFile);
            System.exit(0);
            return;
        }

        create(sourceFile);

        final File targetFile = new File("vouchers.yml");

        if (targetFile.exists()) {
            try {
                Files.write(targetFile.toPath(), new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                LOGGER.error("Could not empty " + targetFile, e);
                System.exit(0);
            }
        } else {
            create(sourceFile);
        }

        try {
            final CommentedConfigurationNode source = YamlConfigurationLoader.builder()
                    .file(sourceFile)
                    .build()
                    .load();

            final YamlConfigurationLoader targetLoader = YamlConfigurationLoader.builder()
                    .file(targetFile)
                    .nodeStyle(NodeStyle.BLOCK)
                    .indent(2)
                    .build();
            final CommentedConfigurationNode target = targetLoader.load();

            if (converter.convert(source, target)) {
                targetLoader.save(target);
            }
        } catch (ConfigurateException e) {
            LOGGER.error("An error has occurred while converting vouchers from " + pluginName, e);
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        final Converter converter = new BadBonesVouchersConverter();

        try {
            final CommentedConfigurationNode source = YamlConfigurationLoader.builder()
                    .file(new File(converter.getFilePath()))
                    .build()
                    .load();

            final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .file(new File("vouchers.yml"))
                    .indent(2)
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();

            final CommentedConfigurationNode target = loader.load();

            if (converter.convert(source, target)) {
                loader.save(target);
            }
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }
    }

}
