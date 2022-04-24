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

    private val ambient: Boolean = meta.properties[AMBIENT]?.toBooleanStrictOrNull() ?: true
    private val particles: Boolean = meta.properties[PARTICLES]?.toBooleanStrictOrNull() ?: true

    /**
     * From **1.9 to 1.12.2**, the [PotionEffect] accepted a [Color] argument
     */
    private val color: Color? = if (ServerVersion.POTION_EFFECT_HAS_COLOR) meta.properties[COLOR]?.toColor() else null

    /**
     * Starting with **1.13**, the [Color] argument was replaced by a boolean for whether the effect should have its
     * icon displayed on the right side of player's screen
     * @see PotionEffect.hasIcon
     */
    private val icon: Boolean = meta.properties[ICON]?.toBooleanStrictOrNull() ?: true

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

        private const val AMBIENT: String = "ambient"
        private const val PARTICLES: String = "particles"
        private const val COLOR: String = "color"
        private const val ICON: String = "icon"

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

        private const val ID: String = "effect"

        private val USAGE: Component = UsageBuilder(ID)
            .hover(Component.text("Give a potion effect to the player"))
            // Optional properties
            .property(AMBIENT) {
                type(UsageBuilder.BOOLEAN)
                    .description("makes potion effect produce more, translucent, particles")
                    .default(true)
            }
            .property(PARTICLES) {
                type(UsageBuilder.BOOLEAN)
                    .description("whether the effect has particles")
                    .default(true)
            }
            // Optional properties with condition
            .property(
                COLOR,
                CONSTRUCTOR_WITH_COLOR != null
            ) {
                type(UsageBuilder.STRING)
                    .description("the color of the particles")
                    .default("none")
            }
            .property(
                ICON,
                ServerVersion.HAS_KEYS,
            ) {
                type(UsageBuilder.BOOLEAN)
                    .description("whether the icon of the effect will be displayed")
                    .default(true)
            }
            // Required arguments
            .argument("effect") {
                type(UsageBuilder.STRING)
                    .description("name of a PotionEffectType")
                    .required()
            }
            .argument("duration") {
                type(UsageBuilder.TICKS)
                    .description("the duration of the effect")
                    .required()
            }
            // Optional arguments
            .argument("amplifier") {
                type(UsageBuilder.INTEGER)
                    .description("the amplifier of the effect")
                    .default("0 (I)")
            }
            .build()

    }

}