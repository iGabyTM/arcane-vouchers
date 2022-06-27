package me.gabytm.minecraft.arcanevouchers.actions.tags

import me.gabytm.util.actions.actions.Context
import me.gabytm.util.actions.components.Component
import me.gabytm.util.actions.placeholders.PlaceholderManager
import org.bukkit.entity.Player
import java.util.*

class ChanceTag(stringValue: String, placeholderManager: PlaceholderManager) : Component<Player, String>(
    stringValue,
    placeholderManager
) {

    private val elements: Set<Pair<String, String>>
    private val default: String

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

        elements = pairs.toSet()
    }

    override fun parse(player: Player, context: Context<Player>): String {
        // Map all elements into Pair<Chance, Element>
        val set = elements.map {
            val chance = placeholderManager.replace(player, it.first, context).toDoubleOrNull()
                ?: throw IllegalArgumentException("(chance tag) '${it.first}' is not a number")
            chance to placeholderManager.replace(player, it.second, context)
        }
        val chance = RANDOM.nextDouble(101.0) // Generate random chance between 0 and 100
        val valid = set.filter { chance <= it.first } // Filter the elements

        return if (valid.isEmpty()) {
            default
        } else if (valid.size == 1) {
            valid.first().second
        } else {
            valid.elementAt(RANDOM.nextInt(valid.size)).second
        }
    }

    @Suppress("unused")
    companion object {

        private const val ID: String = "chance"
        private val RANDOM = SplittableRandom()

    }

}