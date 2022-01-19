package me.gabytm.minecraft.arcanevouchers.functions

import com.google.common.primitives.Ints
import me.gabytm.minecraft.arcanevouchers.Constant
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.ChatColor
import org.apache.commons.lang.StringUtils
import org.bukkit.Color
import sh.okx.timeapi.TimeAPI
import java.util.concurrent.TimeUnit

fun String.color(hex: Boolean = false): String {
    return ChatColor.translateAlternateColorCodes('&', this)
}

fun String.replace(args: Map<String, String>): String {
    return StringUtils.replaceEach(this, args.keys.toTypedArray(), args.values.toTypedArray())
}

fun String.replace(placeholders: Array<String>, values: Array<String>): String {
    return StringUtils.replaceEach(this, placeholders, values)
}

fun String.mini(): Component = Constant.MINI.parse(this)

fun Array<String>.toArgsMap(): MutableMap<String, String> {
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