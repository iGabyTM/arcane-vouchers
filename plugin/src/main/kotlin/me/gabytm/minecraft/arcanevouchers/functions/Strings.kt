package me.gabytm.minecraft.arcanevouchers.functions

import me.gabytm.minecraft.arcanevouchers.Constant
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.ChatColor
import org.apache.commons.lang.StringUtils
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