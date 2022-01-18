package me.gabytm.minecraft.arcanevouchers.comet.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Numbers {

    private Numbers() { }

    @Contract("_, !null -> !null")
    @Nullable
    public static Short tryParseShort(@NotNull final String string, @Nullable final Short def) {
        try {
            return Short.parseShort(string);
        } catch (NumberFormatException ignored) {
            return def;
        }
    }

}
