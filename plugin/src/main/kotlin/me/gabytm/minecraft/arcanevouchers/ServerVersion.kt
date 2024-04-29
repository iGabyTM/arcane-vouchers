package me.gabytm.minecraft.arcanevouchers

import org.bukkit.Bukkit
import org.bukkit.Server
import java.util.regex.Pattern

/**
 * @author [Matt (@LichtHund)](https://github.com/LichtHund)
 */
object ServerVersion {

    private val VERSION = getCurrentVersion()

    private const val V_1_8 = 1_8_0
    private const val V_1_9 = 1_9_0
    private const val V_1_13 = 1_13_0

    val CURRENT = getCurrentVersionAsString()

    val IS_VERY_OLD = CURRENT.startsWith("1.7")
    val IS_ANCIENT = VERSION <= V_1_8

    /**
     * Whether the current version has [org.bukkit.inventory.EquipmentSlot.OFF_HAND] (>= 1.9)
     */
    val HAS_OFF_HAND = VERSION >= V_1_9

    /**
     * Whether in the current version, [org.bukkit.potion.PotionEffect] has a [org.bukkit.Color] argument (1.9 - 1.12.2)
     */
    val POTION_EFFECT_HAS_COLOR = VERSION in 1_9_0..1_12_2

    /**
     * Whether setting an [org.bukkit.inventory.ItemStack] amount to 0 would remove it (>= 1.11.2)
     */
    val ITEMS_WITH_ZERO_AMOUNT_ARE_REMOVED = VERSION >= 1_11_2

    /**
     * Whether the current version is before 1.13
     */
    val IS_LEGACY = VERSION < V_1_13

    /**
     * Whether the current version has the [org.bukkit.NamespacedKey] class
     */
    val HAS_KEYS = VERSION >= V_1_13

    /**
     * Whether in the current version, [org.bukkit.inventory.meta.ItemMeta.getCustomModelData] exists (>= 1.14.4)
     */
    val HAS_CUSTOM_MODEL_DATA = VERSION >= 1_14_14

    /**
     * Whether in the current version [java.util.UUID] can be stored on NBT
     */
    val HAS_UUID_NBT_COMPOUND = VERSION >= 1_16_1

    /**
     * Gets the current server version
     * @return A protocol like number representing the version, for example 1.16.5 - 1165
     */
    private fun getCurrentVersion(): Int {
        // No need to cache since will only run once
        val matcher = Pattern.compile("(?<version>\\d+\\.\\d+)(?<patch>\\.\\d+)?").matcher(getMinecraftVersion())

        return buildString {
            if (matcher.find()) {
                append(matcher.group("version").replace(".", ""))
                append(matcher.group("patch")?.replace(".", "") ?: "0")
            }
        }.toIntOrNull() ?: throw IllegalArgumentException("Could not retrieve server version!")
    }

    private fun getCurrentVersionAsString(): String {
        val matcher = Pattern.compile("\\d+\\.\\d+(?:\\.\\d+)?").matcher(getMinecraftVersion())
        return if (matcher.find()) matcher.group() else "unknown"
    }

    private fun getMinecraftVersion(): String {
        try {
            // Paper method
            val method = Server::class.java.getDeclaredMethod("getMinecraftVersion")
            return method.invoke(Bukkit.getServer()) as String
        } catch (ignored: NoSuchMethodError) {
            return Bukkit.getServer().javaClass.`package`.name.substringAfterLast('.');
        }
    }

    /**
     * Gets a NMS class by its name
     * @return class
     * @throws ClassNotFoundException if the class wasn't found
     */
    @Throws(ClassNotFoundException::class)
    fun getCraftClass(name: String): Class<*> {
        return Class.forName("${Bukkit.getServer().javaClass.`package`.name}.$name")
    }

}