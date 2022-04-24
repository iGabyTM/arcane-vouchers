package me.gabytm.minecraft.arcanevouchers.actions.implementations.other

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.UsageBuilder
import me.gabytm.minecraft.arcanevouchers.actions.implementations.message.Broadcast
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.exception
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.Source
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class SoundAction(meta: ActionMeta<Player>, handler: PermissionHandler) : ArcaneAction(meta, handler) {

    private val broadcast: Broadcast = Broadcast.parse(meta.properties["broadcast"])
    private val source: Source = meta.getProperty("source", Source.MASTER) {
        Source.NAMES.value(it.lowercase())
    }
    private val volume: Float = meta.getProperty("volume", 1f) { it.toFloatOrNull() }
    private val pitch: Float = meta.getProperty("pitch", 1f) { it.toFloatOrNull() }

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

    @Suppress("unused")
    companion object {

        private const val BROADCAST: String = "broadcast"
        private const val SOURCE: String = "source"
        private const val VOLUME: String = "volume"
        private const val PITCH: String = "pitch"

        private const val ID: String = "sound"

        private val USAGE: Component = UsageBuilder(ID)
            .hover(Component.text("Play a sound to the player"))
            // Optional properties
            .property(SOURCE) {
                type(UsageBuilder.STRING)
                    .description("the name of a sound Source")
                    .default(Source.MASTER.name)
            }
            .property(VOLUME) {
                type(UsageBuilder.FLOAT)
                    .description("the volume of the sound")
                    .default(0)
            }
            .property(PITCH) {
                type(UsageBuilder.FLOAT)
                    .description("the pitch of the sound")
                    .default(0)
            }
            // Required arguments
            .argument("sound") {
                type(UsageBuilder.NAMESPACED_KEY)
                    .description("the key of the sound")
                    .required()
            }
            .build()

    }

}