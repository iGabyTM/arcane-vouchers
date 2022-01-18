package me.gabytm.minecraft.arcanevouchers.comet.utils;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public final class Files {

    private static final Logger LOGGER = LoggerFactory.getLogger(Files.class);

    private Files() { }

    public static void copy(@NotNull final String file) {
        try {
            java.nio.file.Files.copy(
                    Paths.get(String.format("./%s", file)),
                    Paths.get(String.format("./copy-%d-%s", System.currentTimeMillis(), file))
            );
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static boolean checkFile(@NotNull final File file) {
        if (file.exists() && file.isFile()) {
            return true;
        }

        LOGGER.warn("Could not find file {} (or it is not one)", file.getName());
        return false;
    }

}
