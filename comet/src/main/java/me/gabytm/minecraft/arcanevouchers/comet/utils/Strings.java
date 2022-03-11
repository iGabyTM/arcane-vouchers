package me.gabytm.minecraft.arcanevouchers.comet.utils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Strings {

    private static final Pattern legacyColorsRegex = Pattern.compile("&(?<code>[0-9a-fk-r])", Pattern.CASE_INSENSITIVE);
    private static final Pattern argsRegex = Pattern.compile("\\{(args(?:\\[\\d+])?)}");

    private Strings() { }

    @Contract("_ -> !null")
    public static String upgradeColorsFormat(@NotNull String string) {
        string = string.replace("\u00A7", "&"); // Replace § with &
        string = argsRegex.matcher(string).replaceAll("%$1%");

        if (legacyColorsRegex.matcher(string).find()) {
            return MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(string));
        }

        return string;
    }

    @Contract("_ -> !null")
    public static List<String> upgradeColorsFormat(@NotNull final List<String> list) {
        return list.stream().map(Strings::upgradeColorsFormat).collect(Collectors.toList());
    }

}
