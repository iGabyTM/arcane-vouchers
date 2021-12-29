package me.gabytm.minecraft.arcanevouchers.updater.utils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Strings {

    private static final Pattern legacyRegex = Pattern.compile("&(?<code>[0-9a-fk-r])", Pattern.CASE_INSENSITIVE);

    private Strings() { }

    public static String upgradeColorsFormat(String string) {
        if (legacyRegex.matcher(string).find()) {
            return MiniMessage.get().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(string));
        }

        return string;
    }

    public static List<String> upgradeColorsFormat(final List<String> list) {
        return list.stream().map(Strings::upgradeColorsFormat).collect(Collectors.toList());
    }

}
