package me.gabytm.minecraft.arcanevouchers.actions.implementations.other

import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.ServerVersion
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.UsageBuilder
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.exception
import me.gabytm.minecraft.arcanevouchers.functions.toColor
import me.gabytm.minecraft.arcanevouchers.functions.warning
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.lang.reflect.Constructor

class EffectAction(meta: ActionMeta<Player>, handler: PermissionHandler) : ArcaneAction(meta, handler) {

    private val ambient: Boolean = meta.properties["ambient"]?.toBooleanStrictOrNull() ?: true
    private val particles: Boolean = meta.properties["particles"]?.toBooleanStrictOrNull() ?: true

    /**
     * From **1.9 to 1.12.2**, the [PotionEffect] accepted a [Color] argument
     */
    private val color: Color? = if (ServerVersion.POTION_EFFECT_HAS_COLOR) {
        meta.properties["color"]?.toColor()
    } else {
        null
    }

    /**
     * Starting with **1.13**, the [Color] argument was replaced by a boolean for `icon`
     * @see PotionEffect.hasIcon
     */
    private val icon: Boolean = meta.properties["icon"]?.toBooleanStrictOrNull() ?: true

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            val parsed = meta.getParsedData(player, context)
            // Format: <effect> <duration> (amplifier: 0) (other properties)
            val parts = parsed.split(Constant.Separator.SPACE)

            if (parts.size < 2) {
                return@execute
            }

            val effect = PotionEffectType.getByName(parts[0])

            if (effect == null) {
                warning("Invalid effect name '${parts[0]}' (effect, '$parsed')")
                return@execute
            }

            val duration = parts[1].toIntOrNull() ?: kotlin.run {
                return@execute
            }

            val amplifier = if (parts.size == 3) {
                parts[2].toIntOrNull() ?: kotlin.run {
                    return@execute
                }
            } else {
                0
            }

            // Check if the server is on 1.8
            if (!ServerVersion.HAS_OFF_HAND) {
                player.addPotionEffect(PotionEffect(effect, duration, amplifier, ambient, particles))
                return@execute
            }

            // The server is on a version between 1.9 and 1.12.2
            val potionEffect = if (ServerVersion.POTION_EFFECT_HAS_COLOR) {
                if (CONSTRUCTOR_WITH_COLOR != null) {
                    CONSTRUCTOR_WITH_COLOR.newInstance(effect, duration, amplifier, ambient, particles, color)
                } else {
                    // The constructor is null, use the one without color argument
                    PotionEffect(effect, duration, amplifier, ambient, particles)
                }
            } else {
                // The server is on a version higher than 1.13
                PotionEffect(effect, duration, amplifier, ambient, particles, icon)
            }

            player.addPotionEffect(potionEffect)
        }
    }

    @Suppress("unused")
    companion object {

        /**
         * On 1.9 - 1.12.2, [PotionEffect] has a [Color] parameter
         */
        private val CONSTRUCTOR_WITH_COLOR: Constructor<PotionEffect>? = if (ServerVersion.POTION_EFFECT_HAS_COLOR) {
            try {
                PotionEffect::class.java.getConstructor(
                    PotionEffectType::class.java,
                    Int::class.java,
                    Int::class.java,
                    Boolean::class.java,
                    Boolean::class.java,
                    Color::class.java
                )
            } catch (e: NoSuchMethodException) {
                exception("Could not find constructor with org.bukkit.Color for org.bukkit.potion.PotionEffect", e)
                null
            }
        } else {
            null
        }

        private const val ID = "effect"

        private val usage = UsageBuilder("effect")
            .hover(Component.text("Give a potion effect to the player"))
            .property(
                UsageBuilder.element("ambient")
                    .type(UsageBuilder.BOOLEAN)
                    .description("makes potion effect produce more, translucent, particles")
                    .default(true)
            )
            .property(
                UsageBuilder.element("particles")
                    .type(UsageBuilder.BOOLEAN)
                    .description("whether the effect has particles")
                    .default(true)
            )
            .property(
                UsageBuilder.element("color")
                    .type(UsageBuilder.STRING)
                    .description("the color of the particles")
                    .default("none"),
                CONSTRUCTOR_WITH_COLOR != null
            )
            .property(
                UsageBuilder.element("icon")
                    .type(UsageBuilder.BOOLEAN)
                    .description("whether the icon of the effect will be displayed")
                    .default(true),
                ServerVersion.HAS_KEYS
            )
            .argument(
                UsageBuilder.element("effect")
                    .type(UsageBuilder.STRING)
                    .description("name of a PotionEffectType")
            )
            .argument(
                UsageBuilder.element("duration")
                    .type(UsageBuilder.TICKS)
                    .description("the duration of the effect")
            )
            .argument(
                UsageBuilder.element("amplifier")
                    .type(UsageBuilder.TICKS)
                    .description("the amplifier of the effect")
                    .default(1)
            )
            .build()

        @JvmStatic
        private fun usage(): Component = usage

    }

}