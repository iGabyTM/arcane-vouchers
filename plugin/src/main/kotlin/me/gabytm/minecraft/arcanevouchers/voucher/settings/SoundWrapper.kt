package me.gabytm.minecraft.arcanevouchers.voucher.settings

import me.gabytm.minecraft.arcanevouchers.functions.exception
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.configuration.ConfigurationSection

class SoundWrapper private constructor(
    private val sound: Sound? = null
) {

    fun play(audience: Audience) {
        audience.playSound(sound ?: return)
    }

    companion object {

        /* no-op */
        val NO_OP = SoundWrapper(null)

        fun from(config: ConfigurationSection?): SoundWrapper {
            if (config == null) {
                return NO_OP
            }

            val soundString = config.getString("sound") ?: return NO_OP

            return try {
                val key = Key.key(soundString)
                val source = config.getString("source")?.let { Sound.Source.NAMES.value(it) } ?: Sound.Source.MASTER
                val volume = config.getDouble("volume", 1.0).toFloat()
                val pitch = config.getDouble("pitch", 1.0).toFloat()

                return SoundWrapper(Sound.sound(key, source, volume, pitch))
            } catch (e: InvalidKeyException) {
                exception("'$soundString' is an invalid Sound namespaced key", e)
                NO_OP
            }
        }

    }

}