package me.gabytm.minecraft.arcanevouchers.actions.implementations.other

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.message.Broadcast
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.exception
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.entity.Player

class SoundAction(meta: ActionMeta<Player>, handler: PermissionHandler) : ArcaneAction(meta, handler) {

    private val broadcast: Broadcast = Broadcast.parse(meta.properties["broadcast"])
    private val source: Sound.Source = meta.getProperty("source", Sound.Source.MASTER) {
        Sound.Source.NAMES.value(it.lowercase())
    }
    private val volume: Float = meta.getProperty("volume", 0f) { it.toFloatOrNull() }
    private val pitch: Float = meta.getProperty("pitch", 0f) { it.toFloatOrNull() }

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            val parsed = meta.getParsedData(player, context)

            try {
                val key = Key.key(parsed)
                val sound = Sound.sound(key, source, volume, pitch)
                broadcast.broadcast(player) { it.playSound(sound) }
            } catch (e: InvalidKeyException) {
                exception("Could not parse namespace key from '$parsed' (sound, ${meta.rawData})", e)
            }
        }
    }

}