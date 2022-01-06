package me.gabytm.minecraft.arcanevouchers

import org.bukkit.Bukkit
import java.util.regex.Pattern

/**
 * @author [Matt (@ipsk)](https://github.com/ipsk)
 */
object ServerVersion {

    private val VERSION = getCurrentVersion()

    private const val V_1_8 = 1_8_0
    private const val V_1_9 = 1_9_0
    private const val V_1_13 = 1_13_0

    val IS_VERY_OLD = VERSION.toString().startsWith("17")
    val IS_ANCIENT = VERSION <= V_1_8
    val HAS_OFF_HAND = VERSION >= V_1_9
    val IS_LEGACY = VERSION < V_1_13

    /**
     * Whether the current version has the [org.bukkit.NamespacedKey] class
     */
    val HAS_KEYS = VERSION >= V_1_13

    /**
     * Gets the current server version
     * @return A protocol like number representing the version, for example 1.16.5 - 1165
     */
    private fun getCurrentVersion(): Int {
        // No need to cache since will only run once
        val matcher = Pattern.compile("(?<version>\\d+\\.\\d+)(?<patch>\\.\\d+)?").matcher(Bukkit.getBukkitVersion())

        return buildString {
            if (matcher.find()) {
                append(matcher.group("version").replace(".", ""))
                append(matcher.group("patch")?.replace(".", "") ?: "0")
            }
        }.toIntOrNull() ?: throw IllegalArgumentException("Could not retrieve server version!")
    }

}