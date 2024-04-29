package me.gabytm.minecraft.arcanevouchers.functions

import com.google.common.base.Enums
import com.google.common.primitives.Ints
import me.clip.placeholderapi.PlaceholderAPI
import me.gabytm.minecraft.arcanevouchers.Constant
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.ParsingException
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.md_5.bungee.api.ChatColor
import org.apache.commons.lang.StringUtils
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.OfflinePlayer
import sh.okx.timeapi.TimeAPI
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

private val EMPTY_COMPONENT_WITHOUT_ITALIC = Component.text().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).build()
private val ARGUMENTS_REGEX = Pattern.compile("([^\"]\\S*|\".+?\")\\s*")
private const val QUOTATION_MARK = '"'

fun String.color(): String {
    return ChatColor.translateAlternateColorCodes('&', this)
}

fun String.replace(args: Map<String, String>): String {
    return this.replace(args.keys.toTypedArray(), args.values.toTypedArray())
}

fun String.replace(placeholders: Array<String>, values: Array<String>): String {
    return StringUtils.replaceEach(this, placeholders, values)
}

fun String.mini(removeItalic: Boolean = false, customTagResolvers: Set<TagResolver> = emptySet()): Component {
    val allTagResolvers = TagResolver.builder()
        .resolvers(TagResolver.standard())
        .resolvers(customTagResolvers)
        .build()

    return try {
        if (removeItalic) {
            EMPTY_COMPONENT_WITHOUT_ITALIC.append(Constant.MINI.deserialize(this, allTagResolvers))
        } else {
            Constant.MINI.deserialize(this, allTagResolvers)
        }
    } catch (e: ParsingException) {
        exception("Could not parse '$this'", e)
        return Component.text(this)
    }
}

fun String.papi(player: OfflinePlayer?): String {
    return if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
        PlaceholderAPI.setPlaceholders(player, this)
    } else {
        this
    }
}

fun Array<String>.toArgsMap(): MutableMap<String, String> {
    if (isEmpty()) {
        return mutableMapOf()
    }

    val map = mutableMapOf("%args%" to joinToString(" "))

    withIndex().forEach { (index, it) -> map["%args[${index + 1}]%"] = it }
    return map
}

fun String.parseTime(unit: TimeUnit): Long {
    if (isEmpty()) {
        return 0L
    }

    return try {
        TimeUnit.MILLISECONDS.convert(TimeAPI(this).milliseconds, unit)
    } catch (e: IllegalArgumentException) {
        warning("Could not parse the time from '$this'")
        -1L
    }
}

/**
 * Attempt to parse a [Color] from a string with format `red,green,blue` or `RRGGBB` (hex)
 * @return [Color] or null
 */
fun String.toColor(): Color? {
    // Try to parse string as a HEX color
    if (length == 6) {
        return try {
            val hex = Ints.tryParse(this, 16) ?: return null
            Color.fromRGB(hex)
        } catch (e: IllegalArgumentException) {
            exception("Could not parse color from hex string '$this'", e)
            null
        }
    }

    val namedColor = Constant.NAMED_COLORS[uppercase()]

    if (namedColor != null) {
        return namedColor
    }

    val parts = split(Constant.Separator.COMMA, 3)

    if (parts.size != 3) {
        return null
    }

    val red = parts[0].toIntOrNull() ?: return null
    val green = parts[1].toIntOrNull() ?: return null
    val blue = parts[2].toIntOrNull() ?: return null

    return try {
        Color.fromRGB(red, green, blue)
    } catch (e: IllegalArgumentException) {
        exception("Could not parse color from '$this'", e)
        null
    }
}

inline fun <reified E: Enum<E>> String.toEnumValue(default: E? = null): E? {
    return Enums.getIfPresent(E::class.java, this.uppercase()).orNull() ?: default
}

fun String.processArguments(): Array<String> {
    // If the string doesn't contain a quote then the regex won't match
    if (!contains(QUOTATION_MARK)) {
        return split(Constant.Separator.SPACE).toTypedArray()
    }

    val arguments = mutableListOf<String>()
    val matcher = ARGUMENTS_REGEX.matcher(this)

    while (matcher.find()) {
        val trimmed = matcher.group().trimEnd()

        // The regex also give the " " that are around the string, so we need to remove them
        if (trimmed.startsWith(QUOTATION_MARK) && trimmed.startsWith(QUOTATION_MARK)) {
            arguments.add(trimmed.substring(1, trimmed.length - 1))
        } else {
            arguments.add(trimmed)
        }
    }

    return arguments.toTypedArray()
}