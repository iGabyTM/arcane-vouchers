package me.gabytm.minecraft.arcanevouchers.actions.tags

import me.gabytm.minecraft.arcanevouchers.functions.debug
import me.gabytm.util.actions.actions.Context
import me.gabytm.util.actions.components.Component
import me.gabytm.util.actions.placeholders.PlaceholderManager
import org.bukkit.entity.Player
import java.util.concurrent.ThreadLocalRandom

class ChanceTag(stringValue: String, placeholderManager: PlaceholderManager) : Component<Player, String>(
    stringValue,
    placeholderManager
) {

    private val elements: Set<Pair<String, String>>
    private val default: String
    private val limitChanceToMax: Boolean

    init {
        // Example: {50=COAL,25=DIAMOND,25=EMERALD,default=STONE}
        val pairs = stringValue
            .split(",") // Split by comma
            .map { it.split("=") } // Split each pair by equal
            .map { it[0] to it[1] } // Create a pair from each part resulted from the previous map { }
            .toMutableSet()

        val defaultValue = pairs.firstOrNull { it.first == "default" }

        default = if (defaultValue != null) {
            pairs.remove(defaultValue)
            defaultValue.second
        } else {
            ""
        }

        limitChanceToMax = default == "#limit-max-chance"

        elements = pairs.toSet()
    }

    override fun parse(player: Player, context: Context<Player>): String {
        // Map all elements into Pair<Chance, Element>
        val set = elements.map {
            val chance = placeholderManager.replace(player, it.first, context).toFloatOrNull()
                ?: throw IllegalArgumentException("(chance tag) '${it.first}' is not a number")
            chance to placeholderManager.replace(player, it.second, context)
        }
        val chance = if (limitChanceToMax) {
            ThreadLocalRandom.current().nextFloat() * set.maxOf { it.first } // Variable max chance
        } else {
            ThreadLocalRandom.current().nextFloat() * 100.00001f // Generate random chance between 0 and 100
        }
        val possible = set.filter { chance <= it.first }
        debug(player, "(chance tag) elements = $elements, set = $set")
        debug(player, "(chance tag) chance = $chance, possible = $possible")

        return if (possible.isEmpty()) {
            default
        } else if (possible.size == 1) {
            possible.first().second
        } else {
            possible.elementAt(ThreadLocalRandom.current().nextInt(possible.size)).second
        }
    }

    @Suppress("unused")
    companion object {

        private const val ID: String = "chance"

    }

}