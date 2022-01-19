package me.gabytm.minecraft.arcanevouchers.comet.modules.implementations.converter;

import me.gabytm.minecraft.arcanevouchers.comet.utils.Files;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

@SuppressWarnings("SameParameterValue")
public abstract class Converter {

    private static final Pattern PATH_SEPARATOR = Pattern.compile("\\.");

    private final String filePath;

    public Converter(String filePath) {
        this.filePath = filePath;
    }

    public final String getFilePath() {
        return filePath;
    }

    @Contract("_ -> !null")
    @NotNull
    protected NodePath processPath(@NotNull final String path) {
        return NodePath.of(PATH_SEPARATOR.split(path));
    }

    protected void moveBoolean(
            @NotNull final CommentedConfigurationNode source, @NotNull final String sourcePath,
            @NotNull final CommentedConfigurationNode target, @NotNull final String targetPath,
            final boolean onlyIfTrue
    ) throws SerializationException {
        source.node(processPath(sourcePath)).act(it -> {
            // Ignore if the value wasn't found
            if (it.virtual()) {
                return;
            }

            final boolean value = it.getBoolean();

            if (onlyIfTrue && !value) {
                return;
            }

            target.node(processPath(targetPath)).set(value);
        });
    }

    protected void moveList(
            @NotNull final CommentedConfigurationNode source, @NotNull final String sourcePath,
            @NotNull final CommentedConfigurationNode target, @NotNull final String targetPath,
            @NotNull final Function<List<String>, List<String>> transformer
    ) throws SerializationException {
        source.node(processPath(sourcePath)).act(it -> {
            if (it.virtual() || it.empty()) {
                return;
            }

            final List<String> list = it.getList(String.class);

            if (list != null) {
                target.node(processPath(targetPath)).setList(String.class, transformer.apply(list));
            }
        });
    }

    protected void moveList(
            @NotNull final CommentedConfigurationNode source, @NotNull final String sourcePath,
            @NotNull final CommentedConfigurationNode target, @NotNull final String targetPath
    ) throws SerializationException {
        moveList(source, sourcePath, target, targetPath, Function.identity());
    }

    protected void moveLong(
            @NotNull final CommentedConfigurationNode source, @NotNull final String sourcePath,
            @NotNull final CommentedConfigurationNode target, @NotNull final String targetPath,
            @NotNull final Function<Long, Long> transformer
    ) throws SerializationException {
        source.node(processPath(sourcePath)).act(it -> {
            if (!it.virtual()) {
                target.node(processPath(targetPath)).set(transformer.apply(it.getLong()));
            }
        });
    }

    protected void moveLong(
            @NotNull final CommentedConfigurationNode source, @NotNull final String sourcePath,
            @NotNull final CommentedConfigurationNode target, @NotNull final String targetPath
    ) throws SerializationException {
        moveLong(source, sourcePath, target, targetPath, Function.identity());
    }

    protected void moveString(
            @NotNull final CommentedConfigurationNode source, @NotNull final String sourcePath,
            @NotNull final CommentedConfigurationNode target, @NotNull final String targetPath,
            @NotNull final Function<String, String> transformer
    ) throws SerializationException {
        source.node(processPath(sourcePath)).act(it -> {
            if (it.virtual() || it.empty()) {
                return;
            }

            final String string = it.getString();

            if (string != null) {
                target.node(processPath(targetPath)).set(transformer.apply(string));
            }
        });
    }

    protected void moveString(
            @NotNull final CommentedConfigurationNode source, @NotNull final String sourcePath,
            @NotNull final CommentedConfigurationNode target, @NotNull final String targetPath
    ) throws SerializationException {
        moveString(source, sourcePath, target, targetPath, Function.identity());
    }

    public abstract boolean convert(final CommentedConfigurationNode source, final CommentedConfigurationNode target) throws SerializationException;

}
